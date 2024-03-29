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

public abstract class BaseMessageBlocker extends PacketAdapter implements MessageBlocker {
    private final ExecutorService executorService;
    private final Set<String> whitelisted;
    private final ProtocolManager manager;

    private final Set<UUID> blocked = new HashSet<>();
    private final Map<UUID, RollingCache<PacketContainer>> messageCache = new ConcurrentHashMap<>();
    private final Map<UUID, String> announcements = new HashMap<>();

    public BaseMessageBlocker(Plugin plugin, ExecutorService executorService, Set<String> whitelisted, ListenerPriority listenerPriority, PacketType... types) {
        super(plugin, listenerPriority, types);
        this.executorService = executorService;
        this.whitelisted = whitelisted;
        this.manager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void init() {
        manager.addPacketListener(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public abstract void onPacketSending(PacketEvent event);

    protected void storePackage(Player player, PacketContainer origPacket) {
        getPlayerCache(player).add(origPacket);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        messageCache.remove(event.getPlayer().getUniqueId());
        blocked.remove(event.getPlayer().getUniqueId());
        announcements.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void blockPlayer(Player player) {
        blocked.add(player.getUniqueId());
        plugin.getLogger().config("Blocking chat for player " + player.getName());
    }

    @Override
    public CompletableFuture<Void> unblockPlayer(Player player) {
        blocked.remove(player.getUniqueId());
        plugin.getLogger().config("Unblocking chat for player " + player.getName());
        var packets = getPlayerCache(player);
        if (packets.isEmpty()) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            for (var blockedPacket : packets.flush()) {
                try {
                    manager.sendServerPacket(player, blockedPacket);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Could not send packet to player", e);
                }
            }
        }, executorService);
    }

    @Override
    public void announce(Player player, String key) {
        announcements.put(player.getUniqueId(), key);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isBlocked(Player player) {
        return blocked.contains(player.getUniqueId());
    }

    @Override
    public Plugin plugin() {
        return plugin;
    }

    @Override
    public boolean isWhitelisted(String value) {
        for (var key : whitelisted) {
            if (value.contains(key)) return true;
        }
        return false;
    }

    public String getAnnouncementKey(Player player) {
        return announcements.get(player.getUniqueId());
    }

    public void removeAnnouncementKey(Player player) {
        announcements.remove(player.getUniqueId());
    }

    @NotNull
    public RollingCache<PacketContainer> getPlayerCache(Player player) {
        return messageCache.computeIfAbsent(player.getUniqueId(), key -> new RollingCache<>(100));
    }
}
