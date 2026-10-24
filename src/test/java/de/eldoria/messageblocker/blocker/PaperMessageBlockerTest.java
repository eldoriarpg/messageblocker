/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperMessageBlockerTest {

    @Test
    void loadsWithoutProtocolLib() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("com.comphenix.protocol.ProtocolLibrary"));
        assertDoesNotThrow(() -> Class.forName(
                PaperMessageBlocker.class.getName(), true, getClass().getClassLoader()));
        assertDoesNotThrow(() -> Class.forName(
                HeldMessage.SystemPacket.class.getName(), true, getClass().getClassLoader()));
    }
}
