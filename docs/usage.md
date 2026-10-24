---
title: Using the blocker
sidebar_position: 3
---

# Using the blocker

Everything you need lives in two types:

- `de.eldoria.messageblocker.MessageBlockerAPI` creates blockers.
- `de.eldoria.messageblocker.blocker.MessageBlocker` is the blocker you work with.

The [Javadoc](pathname:///message-blocker/javadoc/) lists every method.

## Create the blocker

Create the blocker once, in `onEnable`, and keep it in a field.

The quick way uses the default settings:

```java
MessageBlocker blocker = MessageBlockerAPI.create(this);
```

`create` returns the same blocker for every call with the same plugin. You can call it again later and get the
blocker you already have.

To change the settings, use the builder:

```java
MessageBlocker blocker = MessageBlockerAPI.builder(this)
        .queueSize(200)
        .interceptSystemMessages(true)
        .build();
```

| Builder method | Default | What it does |
|---|---|---|
| `queueSize(int)` | `100` | How many messages are held per player. When the queue is full, the oldest message is dropped. Must be at least 1. |
| `executor(Executor)` | the calling thread | Where held messages are sent when you unblock a player. |
| `interceptSystemMessages(boolean)` | `true` | Hold system messages and disguised chat too. Only works with ProtocolLib. |

:::warning
Every call of `build()` makes a new blocker with its own listeners. Build only one blocker per plugin.
`create` does not know about a blocker you built yourself: calling `create` after `build()` gives you a second
blocker.
:::

Create the blocker in `onEnable`, not in the constructor or `onLoad`. It registers event listeners on your
plugin, and it checks whether ProtocolLib is enabled at that moment.

## Block a player

```java
blocker.blockPlayer(player);
```

From now on, messages for this player are held. Call it right before you show your menu.

Blocking a player who is already blocked does nothing more.

## Send your menu

Send everything the player has to see while blocked with `sendBypassing`:

```java
blocker.sendBypassing(player, Component.text("Pick a color:"));
```

These messages are never held.

:::tip
Use `sendBypassing` for every line of your menu, not `player.sendMessage`. With ProtocolLib installed, a plain
`player.sendMessage` to a blocked player counts as a system message and is held like any other.
:::

There is no whitelist of words or senders. `sendBypassing` is the one way to get a message past the blocker.

## Unblock a player

```java
blocker.unblockPlayer(player);
```

This stops holding messages and sends everything that was held, in the order it arrived.

`unblockPlayer` returns a `CompletableFuture<Void>`. It completes once all held messages were sent. Use it to send
something after the old messages, so it ends up at the bottom of the chat:

```java
blocker.unblockPlayer(player)
        .thenRun(() -> player.sendMessage(Component.text("Settings saved.")));
```

By default, the held messages are sent on the thread that calls `unblockPlayer`, and the future is already
complete when the call returns. Set an `executor` on the builder to send them somewhere else.

Messages that arrive while the held ones are being sent wait and come right after them. A message never
overtakes an older one.

Always unblock the player when your menu closes, also when they cancel it or run out of time. A player stays
blocked until you unblock them or they leave.

## Check the state

| Method | Returns |
|---|---|
| `isBlocked(Player)` | `true` while the player is blocked |
| `isActive()` | `true` if this blocker really holds messages, `false` for a dummy |
| `plugin()` | The plugin that owns the blocker |

## Dummy blockers

On a server without Paper's chat API, `build()` and `create` return a dummy instead and log a warning. A dummy
has the same methods but never holds anything:

- `blockPlayer` does nothing,
- `unblockPlayer` returns a future that is already complete,
- `isBlocked` and `isActive` always return `false`,
- `sendBypassing` simply sends the message.

So your code works the same either way. The library needs Paper, though: a server without Paper usually has no
Adventure either, and there `sendBypassing` fails.

You can also make a dummy yourself with `MessageBlocker.dummy(plugin)`, for example to turn the blocker off with a
setting of your own.

Two helpers run code only when the blocker is active:

```java
blocker.ifEnabled(() -> player.sendActionBar(Component.text("Chat is paused")));

Component title = blocker.ifEnabled(Component.text("Settings"),
        text -> text.append(Component.text(" (chat paused)")));
```

`ifEnabled(Runnable)` runs the code if `isActive()` is `true`. `ifEnabled(value, mapper)` returns the mapped value
if the blocker is active and the value unchanged otherwise.

## Threads

All methods of `MessageBlocker` may be called from any thread.

## When things end

- **A blocked player leaves.** Their held messages are thrown away and they are no longer blocked. If they join
  again, block them again if needed.
- **Your plugin is disabled.** Every held message is sent to the players who are still online, right away.

See [Full example](./example.md) for all of this in one small plugin.
