package de.eldoria.messageblocker;

import de.eldoria.messageblocker.blocker.MessageBlocker;
import de.eldoria.messageblocker.blocker.MessageBlockerImpl;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class MessageBlockerBuilder {

    private Plugin plugin;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Set<String> whitelisted = new HashSet<>();

    public MessageBlockerBuilder(Plugin plugin) {
        this.plugin = plugin;
    }

    public MessageBlockerBuilder addWhitelisted(String... terms) {
        whitelisted.addAll(Arrays.asList(terms));
        return this;
    }

    public MessageBlockerBuilder withExectuor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public MessageBlocker build() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            return MessageBlockerAPI.register(new MessageBlockerImpl(plugin, executor, whitelisted));
        }
        return MessageBlocker.dummy(plugin);
    }
}
