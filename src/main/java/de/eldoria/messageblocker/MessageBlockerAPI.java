package de.eldoria.messageblocker;

import de.eldoria.messageblocker.blocker.MessageBlocker;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entry point for creating message blockers.
 */
public final class MessageBlockerAPI {
    private static final Map<Class<? extends Plugin>, MessageBlocker> BLOCKERS = new ConcurrentHashMap<>();

    private MessageBlockerAPI() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Creates a message blocker with default settings. Returns the same instance for every call with the same
     * plugin.
     *
     * @param plugin owning plugin
     * @return message blocker of the plugin
     */
    public static MessageBlocker create(Plugin plugin) {
        return BLOCKERS.computeIfAbsent(plugin.getClass(), key -> builder(plugin).build());
    }

    /**
     * Creates a new builder. Every built blocker registers its own listeners, so build only one per plugin.
     *
     * @param plugin owning plugin
     * @return new builder
     */
    public static MessageBlockerBuilder builder(Plugin plugin) {
        return new MessageBlockerBuilder(plugin);
    }
}
