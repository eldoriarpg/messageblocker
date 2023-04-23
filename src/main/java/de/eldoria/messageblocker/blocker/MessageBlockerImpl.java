package de.eldoria.messageblocker.blocker;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.ExecutorService;

public class MessageBlockerImpl extends BaseMessageBlocker {

    public MessageBlockerImpl(Plugin plugin, ExecutorService executorService, Set<String> whitelisted) {
        super(plugin, executorService, whitelisted, ListenerPriority.HIGHEST, PacketType.Play.Server.CHAT, PacketType.Play.Server.SYSTEM_CHAT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (!isBlocked(event.getPlayer())) {
            // with chat signing we cant send the same message twice.
            // Therefore, we do not store already send packets
            // storePackage(event.getPlayer(), event.getPacket());
            return;
        }

        if (event.getPacket().getType() == PacketType.Play.Server.SYSTEM_CHAT) {
            var message = AdventureComponentAdapter.rawMessage(event.getPacket());
            var announceKey = getAnnouncementKey(event.getPlayer());
            if ((announceKey != null && message.contains(announceKey)) || isWhitelisted(message)) {
                plugin.getLogger().config("Found announce key in message.");
                removeAnnouncementKey(event.getPlayer());
                return;
            }
            plugin.getLogger().config("Blocked system message for " + event.getPlayer().getName() + ": " + message);
        } else {
            plugin.getLogger().config("Blocked player message for " + event.getPlayer().getName());
        }


        storePackage(event.getPlayer(), event.getPacket());
        event.setCancelled(true);
    }
}
