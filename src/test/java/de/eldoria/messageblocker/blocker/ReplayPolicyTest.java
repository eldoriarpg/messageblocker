/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static de.eldoria.messageblocker.blocker.ReplayPolicy.Mode.DISGUISED;
import static de.eldoria.messageblocker.blocker.ReplayPolicy.Mode.SIGNED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplayPolicyTest {
    private static final Instant SIGNED_AT = Instant.parse("2026-10-22T12:00:00Z");
    private static final Instant CAPTURED_AT = SIGNED_AT.plusMillis(50);
    private static final Instant NOW = SIGNED_AT.plus(Duration.ofMinutes(1));

    @Test
    void replaysSignedWhenNothingChanged() {
        assertEquals(SIGNED, ReplayPolicy.decide(true, SIGNED_AT, CAPTURED_AT, null, NOW));
    }

    @Test
    void disguisesWhenSenderSessionChanged() {
        assertEquals(DISGUISED, ReplayPolicy.decide(false, SIGNED_AT, CAPTURED_AT, null, NOW));
    }

    @Test
    void disguisesWhenSignatureExpired() {
        var now = SIGNED_AT.plus(ReplayPolicy.SIGNATURE_LIFETIME);
        assertEquals(DISGUISED, ReplayPolicy.decide(true, SIGNED_AT, CAPTURED_AT, null, now));
    }

    @Test
    void replaysSignedJustBeforeExpiry() {
        var now = SIGNED_AT.plus(ReplayPolicy.SIGNATURE_LIFETIME).minusMillis(1);
        assertEquals(SIGNED, ReplayPolicy.decide(true, SIGNED_AT, CAPTURED_AT, null, now));
    }

    @Test
    void disguisesWhenNewerSignedMessageReachedViewer() {
        var direct = CAPTURED_AT.plusSeconds(10);
        assertEquals(DISGUISED, ReplayPolicy.decide(true, SIGNED_AT, CAPTURED_AT, direct, NOW));
    }

    @Test
    void disguisesWhenDirectMessageHasSameCaptureTime() {
        assertEquals(DISGUISED, ReplayPolicy.decide(true, SIGNED_AT, CAPTURED_AT, CAPTURED_AT, NOW));
    }

    @Test
    void replaysSignedWhenDirectMessageWasOlder() {
        var direct = CAPTURED_AT.minusSeconds(10);
        assertEquals(SIGNED, ReplayPolicy.decide(true, SIGNED_AT, CAPTURED_AT, direct, NOW));
    }
}
