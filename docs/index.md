---
title: Message Blocker
sidebar_position: 1
---

# Message Blocker

Message Blocker is a small library for Paper plugins. It pauses all other chat for a player while your plugin
shows them a chat menu. When you are done, the player gets every message they missed, in the order it arrived.

You shade it into your own plugin. It is not a plugin that server owners install.

## Why you want it

Chat menus are lists of clickable lines in chat. Think of a settings menu, a list of choices or a question like
"Type a name into chat". On a busy server, other messages push your menu out of view before the player can use
it. The player has to scroll up or type the command again.

Message Blocker fixes that. While a player is blocked:

- chat from other players is held back for them,
- with [ProtocolLib](./what-is-held.md) installed, system messages are held back too,
- your own messages still reach them when you send them through the blocker.

Once you unblock the player, the held messages arrive. Up to 100 messages are kept per player, and you can change
that limit.

## A first look

```java
MessageBlocker blocker = MessageBlockerAPI.create(plugin);

blocker.blockPlayer(player);
blocker.sendBypassing(player, Component.text("Type a name into chat"));

blocker.unblockPlayer(player).thenRun(() -> player.sendMessage(Component.text("Done")));
```

## Where to go next

- [Setup](./setup.md): requirements and how to add the library to your build.
- [Using the blocker](./usage.md): create a blocker, block and unblock players, send your menu.
- [What is held](./what-is-held.md): player chat, signed chat, system messages and what ProtocolLib changes.
- [What server owners see](./server-owners.md): the log lines your plugin may print.
- [Full example](./example.md): a small plugin that asks for a nickname in chat.
- [Migrating from 1.x](./migrating.md): what changed in version 2.
