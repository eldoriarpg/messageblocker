package de.eldoria.messageblocker;

import de.eldoria.messageblocker.blocker.IMessageBlockerService;
import de.eldoria.messageblocker.blocker.MessageBlockerService;
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

    public IMessageBlockerService build() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            return MessageBlockerAPI.register(new MessageBlockerService(plugin, executor, whitelisted));
        }
        return IMessageBlockerService.dummy(plugin);
    }
}
