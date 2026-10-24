/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

/**
 * Holds back chat for players while a plugin shows them a chat menu.
 * <p>
 * ProtocolLib has no module name and is read from the classpath, so the build adds
 * {@code --add-reads de.eldoria.messageblocker=ALL-UNNAMED}.
 */
module de.eldoria.messageblocker {
    requires java.logging;
    requires net.kyori.adventure;
    requires net.kyori.examination.api;
    requires org.bukkit;
    requires static org.jetbrains.annotations;

    exports de.eldoria.messageblocker;
    exports de.eldoria.messageblocker.blocker;
}
