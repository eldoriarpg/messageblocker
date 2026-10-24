/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BypassMarkerTest {

    @Test
    void markedComponentSerializesWithToken() {
        var marked = BypassMarker.mark(Component.text("hello"));

        assertTrue(BypassMarker.isMarked(GsonComponentSerializer.gson().serialize(marked)));
    }

    @Test
    void unmarkedComponentHasNoToken() {
        assertFalse(BypassMarker.isMarked(GsonComponentSerializer.gson().serialize(Component.text("hello"))));
    }

    @Test
    void markerAddsNoVisibleText() {
        var marked = BypassMarker.mark(Component.text("hello"));

        assertEquals("hello", PlainTextComponentSerializer.plainText().serialize(marked));
    }
}
