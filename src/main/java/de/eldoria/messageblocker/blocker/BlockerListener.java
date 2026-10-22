package de.eldoria.messageblocker.blocker;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

/**
 * Bukkit listener of a {@link PaperMessageBlocker}.
 * <p>
 * Chat is held at {@link EventPriority#HIGHEST}, after other plugins changed viewers and renderer, and only if
 * no plugin cancelled the event.
 */
final class BlockerListener implements Listener {
    private final PaperMessageBlocker blocker;

    /**
     * Creates a listener.
     *
     * @param blocker blocker to notify
     */
    BlockerListener(PaperMessageBlocker blocker) {
        this.blocker = blocker;
    }

    /**
     * Holds chat for blocked viewers.
     *
     * @param event chat event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        blocker.holdChat(event);
    }

    /**
     * Drops held messages of players leaving.
     *
     * @param event quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        blocker.drop(event.getPlayer());
    }

    /**
     * Delivers held messages when the owning plugin is disabled.
     *
     * @param event disable event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() == blocker.plugin()) blocker.shutdown();
    }
}
