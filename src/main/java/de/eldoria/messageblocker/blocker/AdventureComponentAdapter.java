package de.eldoria.messageblocker.blocker;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
                    var textImpl = field.get(container.getHandle());
                    if (textImpl != null) {
                        var textInterface = Arrays.stream(textImpl.getClass().getInterfaces())
                                .filter(clazz -> clazz.getSimpleName().startsWith("TextComponent"))
                                .findFirst();
                        if (textInterface.isPresent()) {
                            var method = textImpl.getClass().getMethod("content");
                            method.setAccessible(true);
                            return (String) method.invoke(textImpl);
                        }
                        return "";
                    }
                } catch (IllegalAccessException e) {
                    Bukkit.getLogger().log(Level.WARNING, "[MessageBlockerAPI] Could not read field value of adventure$message", e);
                } catch (NoSuchMethodException e) {
                    Bukkit.getLogger().log(Level.WARNING, "[MessageBlockerAPI] Could not read field value of adventure$message", e);
                } catch (InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.WARNING, "[MessageBlockerAPI] Could not read value value of adventure$message", e);
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
