package de.eldoria.messageblocker.blocker;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class AdventureComponentAdapter {
    private AdventureComponentAdapter() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String rawMessage(PacketContainer packet) {
        return adapter.apply(packet);
    }

    private static String adapter(PacketContainer packet) {
        buildAdapter(packet);
        return adapter.apply(packet);
    }

    private static Function<PacketContainer, String> adapter = AdventureComponentAdapter::adapter;

    private static void buildAdapter(PacketContainer packet) {
        try {
            var field = packet.getHandle().getClass().getField("adventure$message");
            adapter = container -> {
                try {
                    var textComponent = field.get(container.getHandle());
                    if (textComponent != null) {
                        return (String) textComponent.getClass().getMethod("content").invoke(textComponent);
                    }
                } catch (IllegalAccessException e) {
                    Bukkit.getLogger().log(Level.WARNING, "[MessageBlockerAPI] Could not read field value of adventure$message");
                } catch (NoSuchMethodException e) {

                } catch (InvocationTargetException e) {

                }
                return getChatComponentText(container).orElseGet(() -> getSafeString(container).orElse(""));
            };
        } catch (NoSuchFieldException e) {
            adapter = r -> getChatComponentText(r).orElseGet(() -> getSafeString(r).orElse(""));
        }
    }

    private static Optional<String> getSafeString(PacketContainer container) {
        if (container.getStrings().size() == 0) {
            return Optional.empty();
        }
        return Optional.of(String.join(" ", container.getStrings().getValues()));
    }

    private static Optional<String> getChatComponentText(PacketContainer container) {
        if (container.getChatComponents().size() == 0) {
            return Optional.empty();
        }
        var components = container.getChatComponents().getValues().stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (components.isEmpty()) {
            return Optional.empty();
        }
        var result = components.stream().map(WrappedChatComponent::getJson).collect(Collectors.joining(""));
        return Optional.of(result);
    }


}
