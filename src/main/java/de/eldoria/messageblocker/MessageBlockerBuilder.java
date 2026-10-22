package de.eldoria.messageblocker;

import de.eldoria.messageblocker.blocker.MessageBlocker;
import de.eldoria.messageblocker.blocker.PaperMessageBlocker;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executor;

/**
 * Builds a {@link MessageBlocker}.
 * <p>
 * On servers without Paper's chat API, {@link #build()} returns a {@link MessageBlocker#dummy(Plugin) dummy}.
 */
public final class MessageBlockerBuilder {
    private static final int DEFAULT_QUEUE_SIZE = 100;

    private final Plugin plugin;
    private int queueSize = DEFAULT_QUEUE_SIZE;
    private Executor executor = Runnable::run;

    /**
     * Creates a new builder.
     *
     * @param plugin owning plugin
     */
    public MessageBlockerBuilder(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets the maximum number of messages held per player. When full, the oldest message is dropped.
     * Defaults to 100.
     *
     * @param queueSize maximum number of held messages, at least 1
     * @return this builder
     */
    public MessageBlockerBuilder queueSize(int queueSize) {
        if (queueSize < 1) throw new IllegalArgumentException("Queue size must be at least 1 but was " + queueSize);
        this.queueSize = queueSize;
        return this;
    }

    /**
     * Sets the executor delivering held messages on unblock. By default they are delivered on the thread calling
     * {@link MessageBlocker#unblockPlayer(org.bukkit.entity.Player)}.
     *
     * @param executor executor
     * @return this builder
     */
    public MessageBlockerBuilder executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Builds the message blocker and registers its listeners.
     *
     * @return the message blocker, or a dummy if the server does not provide Paper's chat API
     */
    public MessageBlocker build() {
        if (!hasPaperChat()) {
            plugin.getLogger().warning("Paper chat API not found. Messages will not be blocked.");
            return MessageBlocker.dummy(plugin);
        }

        return PaperMessageBlocker.start(plugin, queueSize, executor);
    }

    private static boolean hasPaperChat() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
