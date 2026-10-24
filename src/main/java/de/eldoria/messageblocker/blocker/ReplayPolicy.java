/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

/**
 * Decides whether a held signed chat message can be replayed with its signature.
 * <p>
 * A client validates the chain of signed messages per sender: messages of one sender have to arrive in order,
 * belong to the sender's current chat session and must not be older than the client's expiry. A held message
 * that would break one of these rules is replayed disguised, as unsigned chat with the same chat type.
 */
final class ReplayPolicy {
    /**
     * Age after which the client treats a signed message as expired.
     */
    static final Duration SIGNATURE_LIFETIME = Duration.ofMinutes(7);

    private ReplayPolicy() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Decides how a held signed message is replayed.
     *
     * @param sameSenderSession true if the sender is still online with the session the message was captured in
     * @param signedAt          timestamp of the message signature
     * @param capturedAt        time the message was held
     * @param lastDirectSigned  latest time a signed message from the same sender reached the viewer directly
     *                          while holding, or null
     * @param now               current time
     * @return the replay mode
     */
    static Mode decide(
            boolean sameSenderSession,
            Instant signedAt,
            Instant capturedAt,
            @Nullable Instant lastDirectSigned,
            Instant now) {
        if (!sameSenderSession) return Mode.DISGUISED;
        if (!signedAt.plus(SIGNATURE_LIFETIME).isAfter(now)) return Mode.DISGUISED;
        if (lastDirectSigned != null && !lastDirectSigned.isBefore(capturedAt)) return Mode.DISGUISED;
        return Mode.SIGNED;
    }

    /**
     * How a held signed message is replayed.
     */
    enum Mode {
        /**
         * Replayed as the original signed message.
         */
        SIGNED,
        /**
         * Replayed as unsigned disguised chat.
         */
        DISGUISED
    }
}
