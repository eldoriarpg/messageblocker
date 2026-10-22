package de.eldoria.messageblocker.blocker;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Holds back chat for players while they are blocked and delivers it once they are unblocked.
 * <p>
 * Player chat is always held. System and disguised chat are held only when ProtocolLib is installed. Messages
 * that have to reach a blocked player are sent with {@link #sendBypassing(Player, Component)}.
 * <p>
 * All methods may be called from any thread.
 */
public interface MessageBlocker {
    /**
     * Creates a message blocker that never blocks anything.
     * <p>
     * {@link #isActive()} returns false, {@link #isBlocked(Player)} always returns false and
     * {@link #sendBypassing(Player, Component)} sends the message directly.
     *
     * @param plugin owning plugin
     * @return new inactive message blocker
     */
    static MessageBlocker dummy(Plugin plugin) {
        return new DummyMessageBlocker(plugin);
    }

    /**
     * Starts holding messages for a player.
     *
     * @param player player to block
     */
    void blockPlayer(Player player);

    /**
     * Stops holding messages for a player and delivers the held messages in the order they arrived.
     * <p>
     * Messages arriving while the held messages are delivered are sent after them.
     *
     * @param player player to unblock
     * @return a future completed once all held messages were sent
     */
    CompletableFuture<Void> unblockPlayer(Player player);

    /**
     * Checks whether messages for a player are currently blocked.
     *
     * @param player player to check
     * @return true if the player is blocked
     */
    boolean isBlocked(Player player);

    /**
     * Indicates whether this blocker actually holds messages.
     *
     * @return true if active, false for a {@link #dummy(Plugin) dummy}
     */
    boolean isActive();

    /**
     * Sends a message to a player, delivering it immediately even while the player is blocked.
     *
     * @param player  receiver
     * @param message message to send
     */
    void sendBypassing(Player player, Component message);

    /**
     * The plugin owning this blocker.
     *
     * @return owning plugin
     */
    Plugin plugin();

    /**
     * Applies a mapping function when the blocker is active.
     *
     * @param value initial value
     * @param map   mapping function to apply
     * @param <T>   type
     * @return the mapped value if active, otherwise the initial value
     */
    default <T> T ifEnabled(T value, Function<T, T> map) {
        if (isActive()) {
            return map.apply(value);
        }
        return value;
    }

    /**
     * Executes the runnable when the blocker is active.
     *
     * @param runnable runnable to execute
     */
    default void ifEnabled(Runnable runnable) {
        if (isActive()) runnable.run();
    }
}
