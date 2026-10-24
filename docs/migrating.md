---
title: Migrating from 1.x
sidebar_position: 7
---

# Migrating from 1.x

Version 2 is a rewrite for Paper. It holds player chat with Paper's chat event, keeps chat signatures valid and
only needs ProtocolLib for system messages.

## Platform

- **Spigot is no longer supported.** You need Paper. The library is built against the Paper API 1.21.11.
- **Java 21.** Version 1 ran on Java 11. Version 2.1 needs Java 21 or newer (2.0.0 needed Java 25).
- **ProtocolLib is optional.** Version 1 returned a dummy without ProtocolLib. Version 2 holds player chat
  without it and only needs ProtocolLib for system messages. See [What is held](./what-is-held.md).
- **Adventure is no longer bundled.** Version 1 brought `adventure-api` along. Version 2 uses the Adventure that
  comes with Paper.

## API changes

| Version 1 | Version 2 |
|---|---|
| `announce(Player, String)` | Removed. Send the message with `sendBypassing(Player, Component)`. |
| `MessageBlockerBuilder#addWhitelisted(String...)` | Removed. Use `sendBypassing` for every message that has to get through. |
| `MessageBlocker#isWhitelisted(String)` | Removed. |
| `MessageBlockerBuilder#withExecutor(ExecutorService)` | `executor(Executor)` |
| `MessageBlockerAPI.register(MessageBlocker)` | Removed. Use `create` or the builder. |
| `MessageBlocker#init()` | Removed. `build()` and `create` register everything. |
| `MessageBlocker` extends `Listener` | No longer. The blocker registers its own listeners. |
| | New: `sendBypassing(Player, Component)` |
| | New: `MessageBlockerBuilder#queueSize(int)` |
| | New: `MessageBlockerBuilder#interceptSystemMessages(boolean)` |

`blockPlayer`, `unblockPlayer`, `isBlocked`, `isActive`, `plugin`, `ifEnabled` and `MessageBlocker.dummy(Plugin)`
are still there.

## Behaviour changes

- **Held messages are sent on the calling thread by default.** Version 1 used a single thread executor of its
  own. Set `executor(...)` on the builder if you need the old behaviour.
- **The queue size can be set.** It is still 100 by default, and the oldest message is still dropped when it is
  full.
- **Messages sent directly to a blocked player may be held.** With ProtocolLib, a plain `player.sendMessage` to a
  blocked player is held. Send your menu with `sendBypassing`.

## Steps

1. Change the dependency version to `2.1.0`. See [Setup](./setup.mdx).
2. Replace every `announce` call and the message that followed it with one `sendBypassing` call.
3. Drop `addWhitelisted` from your builder. Send those messages with `sendBypassing` instead.
4. Rename `withExecutor` to `executor`, or drop it.
5. Remove calls to `register` and `init`.
6. Keep ProtocolLib as `softdepend` if you want system messages held.

## Changes in 2.1

- **Java 21.** 2.0.0 was compiled for Java 25. 2.1 runs on Java 21 or newer.
- **Deprecated:** `new MessageBlockerBuilder(plugin)` and `PaperMessageBlocker.start(...)`. Use
  `MessageBlockerAPI.builder(plugin)` or `MessageBlockerAPI.create(plugin)`. Both go away in 3.0.
