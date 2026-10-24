/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */
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
    private static final String PROTOCOL_LIB = "ProtocolLib";
    private static final int DEFAULT_QUEUE_SIZE = 100;

    private final Plugin plugin;
    private int queueSize = DEFAULT_QUEUE_SIZE;
    private Executor executor = Runnable::run;
    private boolean interceptSystemMessages = true;

    /**
     * Creates a new builder.
     *
     * @param plugin owning plugin
     * @deprecated use {@link MessageBlockerAPI#builder(Plugin)}. Will be removed in 3.0.
     */
    @Deprecated(since = "2.1.0", forRemoval = true)
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
     * Sets whether system messages are held as well. This requires ProtocolLib and is enabled by default.
     * Without ProtocolLib only player chat is held.
     *
     * @param interceptSystemMessages true to hold system messages
     * @return this builder
     */
    public MessageBlockerBuilder interceptSystemMessages(boolean interceptSystemMessages) {
        this.interceptSystemMessages = interceptSystemMessages;
        return this;
    }

    /**
     * Builds the message blocker and registers its listeners.
     *
     * @return the message blocker, or a dummy if the server does not provide Paper's chat API
     */
    @SuppressWarnings("removal")
    public MessageBlocker build() {
        if (!hasPaperChat()) {
            plugin.getLogger().warning("Paper chat API not found. Messages will not be blocked.");
            return MessageBlocker.dummy(plugin);
        }

        boolean protocolLib =
                interceptSystemMessages && plugin.getServer().getPluginManager().isPluginEnabled(PROTOCOL_LIB);
        if (interceptSystemMessages && !declaresProtocolLib()) {
            plugin.getLogger()
                    .warning(
                            "ProtocolLib is not listed as depend or softdepend. System messages are only held when ProtocolLib is enabled before this plugin.");
        }
        if (interceptSystemMessages && !protocolLib) {
            plugin.getLogger().info("ProtocolLib is not enabled. Only player chat will be held.");
        }

        return PaperMessageBlocker.start(plugin, queueSize, executor, protocolLib);
    }

    private boolean declaresProtocolLib() {
        var meta = plugin.getPluginMeta();
        return meta.getPluginDependencies().contains(PROTOCOL_LIB)
                || meta.getPluginSoftDependencies().contains(PROTOCOL_LIB);
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
