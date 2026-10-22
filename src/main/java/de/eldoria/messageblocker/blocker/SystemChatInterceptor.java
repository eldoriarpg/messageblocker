package de.eldoria.messageblocker.blocker;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Holds system chat and disguised chat packets of holding viewers. Requires ProtocolLib.
 * <p>
 * Action bar messages and components carrying the {@link BypassMarker} pass. Held packets are resent without
 * running packet listeners again.
 * <p>
 * Player chat packets are never cancelled: by the time they are sent, the server has already advanced its chat
 * state for the viewer. They are only observed to record the sender of every signed message that reaches a
 * holding viewer directly, for example from signed commands, which do not pass through the chat event.
 */
final class SystemChatInterceptor extends PacketAdapter {
    private final ProtocolManager manager;
    private final Function<Player, @Nullable ViewerQueue<HeldMessage>> holdingQueue;
    private final AtomicBoolean unreadableReported = new AtomicBoolean();

    /**
     * Creates a new interceptor.
     *
     * @param plugin       owning plugin
     * @param holdingQueue resolves the queue of a viewer if it is holding, otherwise null
     */
    SystemChatInterceptor(Plugin plugin, Function<Player, @Nullable ViewerQueue<HeldMessage>> holdingQueue) {
        super(plugin, ListenerPriority.HIGHEST,
                PacketType.Play.Server.SYSTEM_CHAT, PacketType.Play.Server.DISGUISED_CHAT, PacketType.Play.Server.CHAT);
        this.manager = ProtocolLibrary.getProtocolManager();
        this.holdingQueue = holdingQueue;
    }

    /**
     * Registers the packet listener.
     */
    void register() {
        manager.addPacketListener(this);
    }

    /**
     * Removes the packet listener.
     */
    void unregister() {
        manager.removePacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled() || event.isPlayerTemporary()) return;
        var queue = holdingQueue.apply(event.getPlayer());
        if (queue == null) return;

        var packet = event.getPacket();
        var type = event.getPacketType();
        if (type == PacketType.Play.Server.CHAT) {
            recordSender(queue, packet);
            return;
        }
        if (type == PacketType.Play.Server.SYSTEM_CHAT && isOverlay(packet)) return;
        if (isBypassing(packet)) return;

        if (queue.hold(new HeldMessage.SystemPacket(packet, Instant.now()))) {
            event.setCancelled(true);
        }
    }

    /**
     * Resends a held packet without running packet listeners.
     *
     * @param viewer receiver
     * @param packet packet captured by this interceptor
     */
    void resend(Player viewer, Object packet) {
        manager.sendServerPacket(viewer, (PacketContainer) packet, false);
    }

    private void recordSender(ViewerQueue<HeldMessage> queue, PacketContainer packet) {
        UUID sender = packet.getUUIDs().readSafely(0);
        if (sender != null) queue.recordDirectSigned(sender, Instant.now());
    }

    private static boolean isOverlay(PacketContainer packet) {
        return Boolean.TRUE.equals(packet.getBooleans().readSafely(0));
    }

    /**
     * Checks the packet content for the bypass marker. Content that cannot be read is let through, so an
     * incompatible ProtocolLib degrades to not holding system messages instead of swallowing them.
     */
    private boolean isBypassing(PacketContainer packet) {
        try {
            WrappedChatComponent content = packet.getChatComponents().readSafely(0);
            return content != null && BypassMarker.isMarked(content.getJson());
        } catch (RuntimeException e) {
            if (unreadableReported.compareAndSet(false, true)) {
                plugin.getLogger().log(Level.WARNING, "Could not read chat packet content. System messages will not be held.", e);
            }
            return true;
        }
    }
}
