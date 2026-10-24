/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

/**
 * A message blocker that never blocks anything.
 *
 * @param plugin owning plugin
 */
record DummyMessageBlocker(Plugin plugin) implements MessageBlocker {
    @Override
    public void blockPlayer(Player player) {}

    @Override
    public CompletableFuture<Void> unblockPlayer(Player player) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isBlocked(Player player) {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void sendBypassing(Player player, Component message) {
        player.sendMessage(message);
    }
}
