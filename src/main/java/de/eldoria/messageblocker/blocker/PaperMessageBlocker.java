package de.eldoria.messageblocker.blocker;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;

/**
 * Message blocker for Paper.
 * <p>
 * Player chat is held by replacing blocked viewers in {@link AsyncChatEvent#viewers()} with a stand-in audience
 * that queues the message instead of letting the server pack it for the viewer. On unblock, held chat is sent
 * through the regular send path, so the server assigns chat indices and signature references at that time. A
 * held message is replayed unsigned when its signature would no longer be accepted by the client.
 * <p>
 * Instances are created by {@code MessageBlockerBuilder}.
 */
public final class PaperMessageBlocker implements MessageBlocker {
    private final Plugin plugin;
    private final int capacity;
    private final Executor executor;
    private final Map<UUID, ViewerQueue<HeldMessage>> queues = new ConcurrentHashMap<>();

    private PaperMessageBlocker(Plugin plugin, int capacity, Executor executor) {
        this.plugin = plugin;
        this.capacity = capacity;
        this.executor = executor;
    }

    /**
     * Creates a blocker and registers its listeners.
     *
     * @param plugin   owning plugin
     * @param capacity maximum number of held messages per player, the oldest is dropped when full
     * @param executor executor delivering held messages on unblock
     * @return the running blocker
     */
    public static PaperMessageBlocker start(Plugin plugin, int capacity, Executor executor) {
        var blocker = new PaperMessageBlocker(plugin, capacity, executor);
        plugin.getServer().getPluginManager().registerEvents(new BlockerListener(blocker), plugin);
        return blocker;
    }

    @Override
    public void blockPlayer(Player player) {
        queues.computeIfAbsent(player.getUniqueId(), uuid -> new ViewerQueue<>(capacity)).block();
        plugin.getLogger().fine(() -> "Blocking chat for player " + player.getName());
    }

    @Override
    public CompletableFuture<Void> unblockPlayer(Player player) {
        var queue = queues.get(player.getUniqueId());
        if (queue == null) return CompletableFuture.completedFuture(null);
        queue.unblock();
        plugin.getLogger().fine(() -> "Unblocking chat for player " + player.getName());
        if (!queue.isHolding()) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> flush(player, queue), executor);
    }

    @Override
    public boolean isBlocked(Player player) {
        var queue = queues.get(player.getUniqueId());
        return queue != null && queue.isBlocked();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void sendBypassing(Player player, Component message) {
        player.sendMessage(message);
    }

    @Override
    public Plugin plugin() {
        return plugin;
    }

    /**
     * Replaces holding viewers of a chat message with stand-ins that queue the message.
     *
     * @param event chat event
     */
    void holdChat(AsyncChatEvent event) {
        if (queues.values().stream().noneMatch(ViewerQueue::isHolding)) return;
        var viewers = event.viewers();
        List<Audience> standIns = new ArrayList<>();
        viewers.removeIf(audience -> {
            if (!(audience instanceof Player viewer)) return false;
            var queue = holdingQueue(viewer);
            if (queue == null) return false;
            standIns.add(new HeldChatAudience(viewer, event.getPlayer(), queue));
            return true;
        });
        viewers.addAll(standIns);
    }

    /**
     * Drops everything held for a player.
     *
     * @param player player that left
     */
    void drop(Player player) {
        var queue = queues.remove(player.getUniqueId());
        if (queue != null) queue.discard();
    }

    /**
     * Delivers everything held to players still online.
     */
    void shutdown() {
        for (var entry : queues.entrySet()) {
            var queue = entry.getValue();
            queue.unblock();
            var player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) flush(player, queue);
        }
        queues.clear();
    }

    private @Nullable ViewerQueue<HeldMessage> holdingQueue(Player player) {
        var queue = queues.get(player.getUniqueId());
        return queue != null && queue.isHolding() ? queue : null;
    }

    private void flush(Player viewer, ViewerQueue<HeldMessage> queue) {
        int replayed = queue.flush((message, directSigned) -> replay(viewer, message, directSigned));
        plugin.getLogger().fine(() -> "Delivered " + replayed + " held messages to " + viewer.getName());
    }

    private void replay(Player viewer, HeldMessage message, Map<UUID, Instant> directSigned) {
        try {
            switch (message) {
                case HeldMessage.SignedChat chat -> replayChat(viewer, chat, directSigned.get(chat.sender()));
                case HeldMessage.SystemPacket packet -> {
                }
            }
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.WARNING, "Could not deliver held message to " + viewer.getName(), e);
        }
    }

    private void replayChat(Player viewer, HeldMessage.SignedChat chat, @Nullable Instant lastDirectSigned) {
        boolean sameSession = plugin.getServer().getPlayer(chat.sender()) == chat.senderAtCapture();
        var mode = ReplayPolicy.decide(sameSession, chat.view().timestamp(), chat.at(), lastDirectSigned, Instant.now());
        if (mode == ReplayPolicy.Mode.SIGNED) {
            viewer.sendMessage(chat.view(), chat.bound());
        } else {
            viewer.sendMessage(chat.renderedContent(), chat.bound());
        }
    }
}
