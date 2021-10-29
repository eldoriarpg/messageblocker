[![Wakatime_Badge](https://wakatime.com/badge/github/eldoriarpg/messageblocker.svg)][wakatime]

[![Publish](https://img.shields.io/github/workflow/status/eldoriarpg/messageblocker/Publish%20to%20Nexus?style=for-the-badge&label=Publish)][publish]
[![Build](https://img.shields.io/github/workflow/status/eldoriarpg/messageblocker/Verify%20state?style=for-the-badge&label=Build)][verify]

[![Releases](https://img.shields.io/nexus/maven-releases/de.eldoria/messageblocker?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][release]
[![Development](https://img.shields.io/nexus/maven-dev/de.eldoria/messageblocker?label=DEV&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][development]
[![Snapshots](https://img.shields.io/nexus/s/de.eldoria/messageblocker?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)][snapshot]

<!-- [![Text](image_link)][link_anchor] -->
<!-- [anchor]: link> -->

# Dependency

**Gradle**

``` kotlin
repositories {
    maven("https://eldonexus.de/repository/maven-public")
}

dependencies {
    implementation("de.eldoria", "messageblocker", "version")
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
    <version>version</version>
</dependency>
```

# Usage

Use `MessageBlockerAPI` to create a messageblocker. You will receive a message blocker instance, which will be a blocker
or a dummy. If ProtocolLib is not active on the server a dummy will be returned which can be used as if the blocker
would run.

## Block messages

To block messages you have to call `MessageBlockerService#blockPlayer(Player)` method.

## Unblock messages

To unblock the `MessageBlockerService#unblockPlayer(Player)`. This will return a future which will be completed when the
player has received all blocked messages.

## Send messages through the blocker

If you want to send a message to the player you have to announce it.\
Use `MessageBlockerService#announce(Player, String)` to announce a message to the blocker. The next message for this
player which contains the string will be send to the player.

As an alternative you can add a plugin prefix or something which will be contained in the message to the whitelist when
you use the builder to create the `MessageBlockerService`.

[wakatime]: https://wakatime.com/badge/github/eldoriarpg/messageblocker

[publish]: https://github.com/eldoriarpg/messageblocker/actions/workflows/publish_to_nexus.yml

[verify]: https://github.com/eldoriarpg/messageblocker/actions/workflows/verify.yml

[release]: https://eldonexus.de/#browse/browse:maven-releases:de%2Feldoria%2Fmessageblocker

[development]: https://eldonexus.de/#browse/browse:maven-dev:de%2Feldoria%2Fmessageblocker

[snapshot]: https://eldonexus.de/#browse/browse:maven-snapshots:de%2Feldoria%2Fmessageblocker
