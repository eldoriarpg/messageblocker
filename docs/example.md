---
title: Full example
sidebar_position: 6
---

# Full example

This small plugin has one command, `/nickname`. It asks the player to type a new nickname into chat. While the
player types, all other chat is paused for them. After they answer, they get the messages they missed and a
confirmation at the bottom.

## plugin.yml

```yaml
name: NicknameExample
version: 1.0.0
main: com.example.nickname.NicknamePlugin
api-version: '1.21'
softdepend:
  - ProtocolLib
commands:
  nickname:
    description: Choose a new nickname
```

## The plugin class

```java
package com.example.nickname;

import de.eldoria.messageblocker.MessageBlockerAPI;
import de.eldoria.messageblocker.blocker.MessageBlocker;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NicknamePlugin extends JavaPlugin implements Listener {
    private final Set<UUID> waiting = ConcurrentHashMap.newKeySet();
    private MessageBlocker blocker;

    @Override
    public void onEnable() {
        blocker = MessageBlockerAPI.create(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        waiting.add(player.getUniqueId());
        blocker.blockPlayer(player);
        blocker.sendBypassing(player, Component.text("Type your new nickname into chat.", NamedTextColor.GOLD));
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!waiting.remove(player.getUniqueId())) return;

        event.setCancelled(true);
        String nickname = PlainTextComponentSerializer.plainText().serialize(event.message());

        getServer().getScheduler().runTask(this, () -> {
            player.displayName(Component.text(nickname));
            blocker.unblockPlayer(player).thenRun(() ->
                    player.sendMessage(Component.text("Your nickname is now " + nickname + ".", NamedTextColor.GREEN)));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        waiting.remove(event.getPlayer().getUniqueId());
    }
}
```

## What happens

1. `onEnable` creates the blocker with the default settings.
2. `/nickname` blocks the player and sends the question with `sendBypassing`, so it is not held.
3. Other players keep chatting. Their messages are held for this player.
4. The player types an answer. The plugin cancels that chat event at `LOWEST`, so nobody else sees the answer
   and the blocker does not touch it.
5. Back on the main thread, the plugin sets the nickname and unblocks the player. The held messages arrive.
6. When they are all sent, the future completes and the confirmation shows up below them.

If the player leaves while waiting, the blocker drops their held messages by itself. The plugin only forgets
that it was waiting for an answer.

A real plugin would also unblock the player after a timeout or when they type something like `cancel`. See
[Using the blocker](./usage.md).
