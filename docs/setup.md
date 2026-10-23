---
title: Setup
sidebar_position: 2
---

# Setup

## Requirements

| What | Version |
|---|---|
| Server | Paper. The library is built against the Paper API 1.21.11. |
| Java | 25. The library is compiled for Java 25, so your plugin and the server need Java 25 as well. |
| ProtocolLib | Optional. Built against ProtocolLib 5.4.0. |

Without ProtocolLib, only player chat is held. With ProtocolLib, system messages and disguised chat are held as
well. See [What is held](./what-is-held.md) for the details.

Adventure comes with Paper. The library does not bring its own copy, and it has no other runtime dependencies.

## Add the dependency

Message Blocker is published to the EldoNexus repository.

| | |
|---|---|
| Repository | `https://eldonexus.de/repository/maven-public/` |
| Group | `de.eldoria` |
| Artifact | `messageblocker` |
| Version | `2.0.0` |

Message Blocker is a library, not a plugin. It has no `plugin.yml`, so server owners cannot install it on their
own. Shade it into your plugin jar.

### Gradle (Kotlin DSL)

Add the dependency with `implementation` and let the [Shadow](https://github.com/GradleUp/shadow) plugin put it
into your jar:

```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("de.eldoria:messageblocker:2.0.0")
}

tasks.shadowJar {
    relocate("de.eldoria.messageblocker", "com.example.myplugin.libs.messageblocker")
}
```

Build your plugin with the `shadowJar` task.

### Maven

```xml
<repositories>
    <repository>
        <id>EldoNexus</id>
        <url>https://eldonexus.de/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.eldoria</groupId>
        <artifactId>messageblocker</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

Shade and relocate it with the `maven-shade-plugin`. Use the current version of the plugin:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>de.eldoria.messageblocker</pattern>
                                <shadedPattern>com.example.myplugin.libs.messageblocker</shadedPattern>
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Why relocate

Other plugins on the same server may shade Message Blocker too, maybe in another version. Moving it into a
package of your own keeps your copy apart from theirs. Replace `com.example.myplugin` with your plugin's package.

Do not shade ProtocolLib. It is a plugin of its own that the server owner installs.

## Declare ProtocolLib

If you want system messages held, list ProtocolLib as `softdepend` (or `depend`) in your `plugin.yml`:

```yaml
softdepend:
  - ProtocolLib
```

This makes the server enable ProtocolLib before your plugin. The blocker only uses ProtocolLib if it is already
enabled when you create the blocker.

If you leave ProtocolLib out of `plugin.yml`, the blocker logs a warning on every start. If you never want
system messages held, turn that off with `interceptSystemMessages(false)` on the
[builder](./usage.md). Then nothing about ProtocolLib is logged.
