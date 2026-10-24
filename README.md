# Message Blocker

A small library for Paper plugins that pauses all other chat for a player while your plugin shows them a chat menu.
When you unblock them, they get every message they missed, in the order it arrived.

[![Release](https://img.shields.io/nexus/maven-releases/de.eldoria/messageblocker?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][release]
[![Snapshot](https://img.shields.io/nexus/s/de.eldoria/messageblocker?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][snapshot]

## Requirements

- Java 21 or newer.
- Paper 1.21.11 or newer.
- ProtocolLib is optional. Without it, only player chat is held.

## Dependency

Message Blocker is published to EldoNexus only. Shade it into your plugin and relocate it.

```kotlin
repositories {
    maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
    implementation("de.eldoria:messageblocker:2.1.0")
}
```

```xml
<repository>
    <id>EldoNexus</id>
    <url>https://eldonexus.de/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>de.eldoria</groupId>
    <artifactId>messageblocker</artifactId>
    <version>2.1.0</version>
</dependency>
```

## Documentation

https://docs.eldoria.de/message-blocker

[release]: https://eldonexus.de/#browse/browse:maven-releases:de%2Feldoria%2Fmessageblocker
[snapshot]: https://eldonexus.de/#browse/browse:maven-snapshots:de%2Feldoria%2Fmessageblocker
