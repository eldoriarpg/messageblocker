/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * Messages held back for a single viewer.
 * <p>
 * The queue holds while the viewer is blocked or while older messages are still waiting, so a message never
 * overtakes one that arrived before it. Once unblocked and drained, callers deliver directly. Flushing replays
 * under the lock, which makes concurrent {@link #hold(Object)} calls wait until the replay is complete and keeps
 * the order of messages per sender intact.
 * <p>
 * The queue is bounded: when full, the oldest message is dropped.
 *
 * @param <T> type of held message
 */
final class ViewerQueue<T> {
    /**
     * Default number of messages held per viewer.
     */
    static final int DEFAULT_CAPACITY = 100;

    private final ReentrantLock lock = new ReentrantLock();
    private final Deque<T> held = new ArrayDeque<>();
    private final Map<UUID, Instant> directSigned = new HashMap<>();
    private final int capacity;
    private boolean blocked;

    /**
     * Creates a new queue.
     *
     * @param capacity maximum number of held messages
     */
    ViewerQueue(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least 1 but was " + capacity);
        this.capacity = capacity;
    }

    /**
     * Starts holding messages.
     */
    void block() {
        lock.lock();
        try {
            blocked = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stops blocking. Messages already held stay queued, and new messages keep queueing behind them until
     * {@link #flush(BiConsumer)} drains the queue.
     */
    void unblock() {
        lock.lock();
        try {
            blocked = false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks whether the viewer is blocked.
     *
     * @return true while blocked
     */
    boolean isBlocked() {
        lock.lock();
        try {
            return blocked;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks whether new messages for this viewer have to be held.
     *
     * @return true while blocked or while messages are waiting
     */
    boolean isHolding() {
        lock.lock();
        try {
            return holding();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Holds a message if the queue is holding.
     *
     * @param message message to hold
     * @return true if the message was queued, false if the caller has to deliver it directly
     */
    boolean hold(T message) {
        lock.lock();
        try {
            if (!holding()) return false;
            if (held.size() >= capacity) held.pollFirst();
            held.addLast(message);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Records that a signed message from a sender reached the viewer directly while the queue was holding.
     * Nothing is recorded when the queue is not holding.
     *
     * @param sender uuid of the sender
     * @param at     time the message was seen
     */
    void recordDirectSigned(UUID sender, Instant at) {
        lock.lock();
        try {
            if (!holding()) return;
            directSigned.merge(sender, at, (prev, next) -> next.isAfter(prev) ? next : prev);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Drains the queue and replays every held message in order, unless the viewer is blocked.
     * <p>
     * The replay runs while the lock is held. The replay function receives the latest time a signed message of
     * each sender reached the viewer directly while holding.
     *
     * @param replay receives every held message and the direct signed deliveries by sender
     * @return number of replayed messages
     */
    int flush(BiConsumer<? super T, Map<UUID, Instant>> replay) {
        lock.lock();
        try {
            if (blocked) return 0;
            List<T> drained = new ArrayList<>(held);
            Map<UUID, Instant> direct = Map.copyOf(directSigned);
            held.clear();
            directSigned.clear();
            for (T message : drained) {
                replay.accept(message, direct);
            }
            return drained.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Drops all held messages and stops blocking.
     */
    void discard() {
        lock.lock();
        try {
            held.clear();
            directSigned.clear();
            blocked = false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Number of held messages.
     *
     * @return size of the queue
     */
    int size() {
        lock.lock();
        try {
            return held.size();
        } finally {
            lock.unlock();
        }
    }

    private boolean holding() {
        return blocked || !held.isEmpty();
    }
}
