[![Wakatime_Badge](https://wakatime.com/badge/github/eldoriarpg/messageblocker.svg)][wakatime]

[![Publish](https://img.shields.io/github/workflow/status/eldoriarpg/messageblocker/Publish%20to%20Nexus?style=for-the-badge&label=Publish)][publish]
[![Build](https://img.shields.io/github/workflow/status/eldoriarpg/messageblocker/Verify%20state?style=for-the-badge&label=Build)][verify]

[![Releases](https://img.shields.io/nexus/maven-releases/de.eldoria/messageblocker?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][release]
[![Development](https://img.shields.io/nexus/maven-dev/de.eldoria/messageblocker?label=DEV&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][development]
[![Snapshots](https://img.shields.io/nexus/s/de.eldoria/messageblocker?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][snapshot]

<!-- [![Text](image_link)][link_anchor] -->
<!-- [anchor]: link> -->

# Dependency

Requires Paper 1.21.11 or newer and Java 25. ProtocolLib is optional.

**Gradle**

``` kotlin
repositories {
    maven("https://eldonexus.de/repository/maven-public")
}

dependencies {
    implementation("de.eldoria", "messageblocker", "2.0.0")
}
```

**Maven**

``` xml
<repository>
    <id>EldoNexus</id>
    <url>https://eldonexus.de/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>de.eldoria</groupId>
    <artifactId>messageblocker</artifactId>
    <version>2.0.0</version>
</dependency>
```

Adventure is provided by Paper and is not shaded.

# Usage

``` java
MessageBlocker blocker = MessageBlockerAPI.create(plugin);

blocker.blockPlayer(player);
blocker.sendBypassing(player, Component.text("Type a name into chat"));
blocker.unblockPlayer(player).thenRun(() -> player.sendMessage(Component.text("Done")));
```

Use `MessageBlockerAPI.create(Plugin)` to get the message blocker of your plugin, or `MessageBlockerAPI.builder(Plugin)`
to configure one:

``` java
MessageBlocker blocker = MessageBlockerAPI.builder(plugin)
        .queueSize(100)
        .executor(executor)
        .interceptSystemMessages(true)
        .build();
```

On a server without Paper's chat API a dummy is returned, which never blocks but can be used like an active blocker.
`MessageBlocker.dummy(Plugin)` creates one directly.

## What is held

- **Player chat** is always held. Blocked players are swapped out of the viewers of Paper's `AsyncChatEvent` and the
  message is queued before the server packs it for them. On unblock it is sent through the regular chat path, so chat
  signing stays valid. A message is delivered as unsigned chat when its signature would no longer be accepted: the sender
  logged out or rejoined, the message is older than seven minutes, or a newer signed message of the same sender, such as
  a `/msg`, already reached the player.
- **System messages and disguised chat** are held when ProtocolLib is installed. Add ProtocolLib as `softdepend` (or
  `depend`) so it is enabled before your plugin. Action bar messages are never held.

Up to 100 messages are held per player by default. When the queue is full, the oldest message is dropped. Held messages
of a player are dropped when the player leaves.

## Block messages

Call `MessageBlocker#blockPlayer(Player)`.

## Unblock messages

Call `MessageBlocker#unblockPlayer(Player)`. It returns a future completed once all held messages were sent. By default
they are sent on the calling thread; use `MessageBlockerBuilder#executor(Executor)` to change that. Messages arriving
while held messages are delivered are sent after them.

## Send messages through the blocker

Send messages that have to reach a blocked player with `MessageBlocker#sendBypassing(Player, Component)`. They are never
held.

## Limitations

- Plugins cancelling `AsyncChatEvent` and broadcasting the message themselves bypass the blocker for player chat.
- Viewer-aware chat renderers receive a stand-in audience instead of the `Player` for blocked viewers. Its pointers
  (UUID, name, display name) are those of the player.

## Migrating from 1.x

- Spigot is no longer supported.
- `announce(Player, String)` and the text whitelist were removed. Use `sendBypassing(Player, Component)`.
- `MessageBlockerBuilder#withExecutor` is now `executor`, `addWhitelisted` was removed.
- ProtocolLib is optional; without it only player chat is held.

[wakatime]: https://wakatime.com/badge/github/eldoriarpg/messageblocker

[publish]: https://github.com/eldoriarpg/messageblocker/actions/workflows/publish_to_nexus.yml

[verify]: https://github.com/eldoriarpg/messageblocker/actions/workflows/verify.yml

[release]: https://eldonexus.de/#browse/browse:maven-releases:de%2Feldoria%2Fmessageblocker

[development]: https://eldonexus.de/#browse/browse:maven-dev:de%2Feldoria%2Fmessageblocker

[snapshot]: https://eldonexus.de/#browse/browse:maven-snapshots:de%2Feldoria%2Fmessageblocker
