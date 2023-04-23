package de.eldoria.messageblocker;

import de.eldoria.messageblocker.blocker.LegacyMessageBlockerImpl;
import de.eldoria.messageblocker.blocker.MessageBlocker;
import de.eldoria.messageblocker.blocker.MessageBlockerImpl;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public final class MessageBlockerAPI {
    private static final Map<Class<? extends Plugin>, MessageBlocker> blocker = new HashMap<>();

    private MessageBlockerAPI() {
    }

    /**
     * Creates a default message blocker. This will always return the same instance after the first call.
     *
     * @param plugin plugin
     * @return message blocker instance of the plugin
     */
    public static MessageBlocker create(Plugin plugin) {
        var descr = plugin.getDescription();
        if (!descr.getDepend().contains("ProtocolLib") || !descr.getSoftDepend().contains("ProtocolLib")) {
            plugin.getLogger().warning("The MessageBlocker API is used, but ProtocolLib is not listed as depend or soft depend.");
        }
        return blocker.computeIfAbsent(plugin.getClass(), k -> init(builder(plugin).build()));
    }

    /**
     * Creates a new builder.
     *
     * @param plugin plugin
     * @return message new builder instance
     */
    public static MessageBlockerBuilder builder(Plugin plugin) {
        return new MessageBlockerBuilder(plugin);
    }

    /**
     * Registers a message blocker. This blocker will only be registered when no builder was registered before.
     *
     * @param messageBlockerService message blocker
     * @return message blocker
     */
    public static MessageBlocker register(MessageBlocker messageBlockerService) {
        return blocker.computeIfAbsent(messageBlockerService.plugin().getClass(), key -> init(messageBlockerService));
    }

    private static MessageBlocker init(MessageBlocker service) {
        service.init();
        return service;
    }
}
