/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewerQueueTest {

    @Test
    void doesNotHoldWhenNeverBlocked() {
        var queue = new ViewerQueue<String>(10);

        assertFalse(queue.hold("a"));
        assertFalse(queue.isHolding());
        assertEquals(0, queue.size());
    }

    @Test
    void holdsInArrivalOrderWhileBlocked() {
        var queue = new ViewerQueue<String>(10);
        queue.block();

        assertTrue(queue.hold("a"));
        assertTrue(queue.hold("b"));
        assertTrue(queue.hold("c"));

        queue.unblock();
        assertEquals(List.of("a", "b", "c"), flush(queue));
    }

    @Test
    void dropsOldestWhenFull() {
        var queue = new ViewerQueue<String>(3);
        queue.block();
        for (var message : List.of("a", "b", "c", "d", "e")) {
            queue.hold(message);
        }

        assertEquals(3, queue.size());
        queue.unblock();
        assertEquals(List.of("c", "d", "e"), flush(queue));
    }

    @Test
    void keepsHoldingAfterUnblockUntilFlushed() {
        var queue = new ViewerQueue<String>(10);
        queue.block();
        queue.hold("a");
        queue.unblock();

        assertFalse(queue.isBlocked());
        assertTrue(queue.isHolding());
        assertTrue(queue.hold("b"));

        assertEquals(List.of("a", "b"), flush(queue));
        assertFalse(queue.isHolding());
        assertFalse(queue.hold("c"));
    }

    @Test
    void flushDoesNothingWhileBlocked() {
        var queue = new ViewerQueue<String>(10);
        queue.block();
        queue.hold("a");

        assertEquals(List.of(), flush(queue));
        assertEquals(1, queue.size());
    }

    @Test
    void reblockBeforeFlushKeepsMessages() {
        var queue = new ViewerQueue<String>(10);
        queue.block();
        queue.hold("a");
        queue.unblock();
        queue.block();

        assertEquals(List.of(), flush(queue));
        queue.unblock();
        assertEquals(List.of("a"), flush(queue));
    }

    @Test
    void discardDropsEverything() {
        var queue = new ViewerQueue<String>(10);
        queue.block();
        queue.hold("a");
        queue.recordDirectSigned(UUID.randomUUID(), Instant.EPOCH);

        queue.discard();

        assertFalse(queue.isHolding());
        assertEquals(0, queue.size());
        assertEquals(List.of(), flush(queue));
    }

    @Test
    void recordsDirectSignedOnlyWhileHolding() {
        var queue = new ViewerQueue<String>(10);
        var sender = UUID.randomUUID();
        queue.recordDirectSigned(sender, Instant.ofEpochSecond(1));

        queue.block();
        queue.hold("a");
        queue.recordDirectSigned(sender, Instant.ofEpochSecond(5));
        queue.recordDirectSigned(sender, Instant.ofEpochSecond(3));
        queue.unblock();

        var seen = new AtomicReference<Map<UUID, Instant>>();
        queue.flush((message, direct) -> seen.set(direct));
        assertEquals(Instant.ofEpochSecond(5), seen.get().get(sender));

        queue.block();
        queue.hold("b");
        queue.unblock();
        queue.flush((message, direct) -> seen.set(direct));
        assertNull(seen.get().get(sender));
    }

    @Test
    void messageArrivingDuringFlushIsDeliveredAfterReplay() throws InterruptedException {
        var queue = new ViewerQueue<String>(10);
        queue.block();
        queue.hold("a");
        queue.hold("b");
        queue.unblock();

        List<String> delivered = Collections.synchronizedList(new ArrayList<>());
        var replayStarted = new CountDownLatch(1);
        var releaseReplay = new CountDownLatch(1);

        var flusher = Thread.ofPlatform()
                .start(() -> queue.flush((message, direct) -> {
                    replayStarted.countDown();
                    await(releaseReplay);
                    delivered.add(message);
                }));
        assertTrue(replayStarted.await(5, TimeUnit.SECONDS));

        var sender = Thread.ofPlatform().start(() -> {
            if (!queue.hold("c")) delivered.add("c");
        });
        assertFalse(sender.join(Duration.ofMillis(200)));

        releaseReplay.countDown();
        flusher.join();
        sender.join();

        assertEquals(List.of("a", "b", "c"), delivered);
        assertEquals(0, queue.size());
    }

    @Test
    void messageArrivingBeforeFlushIsReplayedInOrder() {
        var queue = new ViewerQueue<String>(10);
        queue.block();
        queue.hold("a");
        queue.unblock();
        assertTrue(queue.hold("b"));

        assertEquals(List.of("a", "b"), flush(queue));
    }

    @Test
    void rejectsInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new ViewerQueue<String>(0));
    }

    private static List<String> flush(ViewerQueue<String> queue) {
        List<String> replayed = new ArrayList<>();
        queue.flush((message, direct) -> replayed.add(message));
        return replayed;
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
