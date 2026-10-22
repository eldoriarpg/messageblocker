package de.eldoria.messageblocker.blocker;

import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.UUID;

/**
 * A message held back for a viewer.
 */
sealed interface HeldMessage permits HeldMessage.SignedChat, HeldMessage.SystemPacket {
    /**
     * Time the message was held.
     *
     * @return capture time
     */
    Instant at();

    /**
     * Player chat captured before the server packed it for the viewer.
     *
     * @param view            the signed message, carrying the content rendered for the viewer as unsigned content
     * @param bound           the bound chat type
     * @param senderAtCapture the sending player instance at capture time
     * @param at              capture time
     */
    record SignedChat(SignedMessage view, ChatType.Bound bound, Player senderAtCapture,
                      Instant at) implements HeldMessage {
        /**
         * Uuid of the sender.
         *
         * @return sender uuid
         */
        public UUID sender() {
            return senderAtCapture.getUniqueId();
        }

        /**
         * The content the viewer would have seen, used for disguised replay.
         *
         * @return rendered content
         */
        public Component renderedContent() {
            Component unsigned = view.unsignedContent();
            return unsigned != null ? unsigned : Component.text(view.message());
        }
    }

    /**
     * A system or disguised chat packet intercepted on its way to the viewer.
     *
     * @param packet the intercepted packet, resent by the interceptor that captured it
     * @param at     capture time
     */
    record SystemPacket(Object packet, Instant at) implements HeldMessage {
    }
}
