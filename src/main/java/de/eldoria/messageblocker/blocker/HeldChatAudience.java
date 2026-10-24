/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
package de.eldoria.messageblocker.blocker;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.pointer.Pointers;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Stands in for a holding viewer in the viewers of a chat event.
 * <p>
 * Paper delivers chat to audiences that are neither players nor forwarding to one by calling
 * {@link #sendMessage(SignedMessage, ChatType.Bound)} with the message rendered for this audience. The message is
 * queued while the viewer is holding and sent to the viewer otherwise. Nothing is packed for the viewer at that
 * point, so the server's chat state for the viewer stays untouched until the message is replayed.
 * <p>
 * The audience deliberately does not implement {@link net.kyori.adventure.audience.ForwardingAudience}, which
 * would make Paper send to the player natively. Its pointers are the pointers of the viewer.
 */
final class HeldChatAudience implements Audience {
    private final Player viewer;
    private final Player sender;
    private final ViewerQueue<HeldMessage> queue;

    /**
     * Creates a stand-in for a viewer.
     *
     * @param viewer the viewer this audience replaces
     * @param sender the player sending the chat message
     * @param queue  the queue of the viewer
     */
    HeldChatAudience(Player viewer, Player sender, ViewerQueue<HeldMessage> queue) {
        this.viewer = viewer;
        this.sender = sender;
        this.queue = queue;
    }

    @Override
    public void sendMessage(@NotNull SignedMessage signedMessage, ChatType.@NotNull Bound boundChatType) {
        var held = new HeldMessage.SignedChat(signedMessage, boundChatType, sender, Instant.now());
        if (!queue.hold(held)) {
            viewer.sendMessage(signedMessage, boundChatType);
        }
    }

    @Override
    public @NotNull Pointers pointers() {
        return viewer.pointers();
    }
}
