package de.eldoria.messageblocker.blocker;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface IMessageBlockerService {
    /**
     * Creates a dummy {@link IMessageBlockerService} which will not block any messages, but will behave like an active message blocker
     *
     * @return new message blocker instance
     */
    static IMessageBlockerService dummy(Plugin plugin) {
        return new IMessageBlockerService() {
            @Override
            public boolean isBlocked(Player player) {
                return false;
            }

            @Override
            public Plugin plugin() {
                return plugin;
            }
        };
    }

    /**
     * Checks if the message contains a generally whitelisted character. This can be used to allow messages identified by a plugin prefix to be delivered.
     *
     * @param value value to check
     * @return true when a whitelisted char combination was found
     */
    default boolean isWhitelisted(String value) {
        return false;
    }

    /**
     * @param player player to block
     */
    default void blockPlayer(Player player) {
    }

    /**
     * Unblock a player from message blocker
     *
     * @param player player to unblock
     * @return a completable future which will be completed once all blocked packets were send
     */
    default CompletableFuture<Void> unblockPlayer(Player player) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Announces that a player will soon receive a message which needs to be let through
     *
     * @param player player which will receive the message
     * @param key    a key inside the message. This needs to be part of the content of the message in case senstive way.
     */
    default void announce(Player player, String key) {
    }

    /**
     * Indicates whether the service is active or not.
     *
     * @return true if active
     */
    default boolean isActive() {
        return false;
    }

    /**
     * Checks if the messages for a player are blocked
     *
     * @param player player to check
     * @return true if the player has blocked messages currently
     */
    boolean isBlocked(Player player);

    Plugin plugin();

    /**
     * Executres the mapping function when the blocker is enabled.
     *
     * @param value initial value
     * @param map   mapping function to apply
     * @param <T>   type
     * @return mapped value or value
     */
    default <T> T ifEnabled(T value, Function<T, T> map) {
        if (isActive()) {
            return map.apply(value);
        }
        return value;
    }
}
