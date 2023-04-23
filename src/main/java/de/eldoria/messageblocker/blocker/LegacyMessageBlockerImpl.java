package de.eldoria.messageblocker.blocker;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.eldoria.messageblocker.util.RollingCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class LegacyMessageBlockerImpl extends BaseMessageBlocker {

    public LegacyMessageBlockerImpl(Plugin plugin, ExecutorService executorService, Set<String> whitelisted) {
        super(plugin, executorService, whitelisted, ListenerPriority.HIGHEST, PacketType.Play.Server.CHAT);
    }


    @Override
    public void onPacketSending(PacketEvent event) {
        if (!isBlocked(event.getPlayer())) {
            storePackage(event.getPlayer(), event.getPacket());
            return;
        }

        var message = AdventureComponentAdapter.rawMessage(event.getPacket());
        var announceKey = getAnnouncementKey(event.getPlayer());
        if ((announceKey != null && message.contains(announceKey)) || isWhitelisted(message)) {
            plugin.getLogger().config("Found announce key in message.");
            removeAnnouncementKey(event.getPlayer());
            return;
        }

        plugin.getLogger().config("Blocked message for " + event.getPlayer().getName() + ": " + message);

        storePackage(event.getPlayer(), event.getPacket());
        event.setCancelled(true);
    }
}
