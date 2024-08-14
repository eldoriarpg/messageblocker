plugins {
    java
    `maven-publish`
    id("de.chojo.publishdata") version "1.4.0"
}

group = "de.eldoria"
version = "1.1.2"

repositories {
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
}

dependencies {
    compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol", "ProtocolLib", "5.0.0-SNAPSHOT")
    compileOnly("org.jetbrains", "annotations", "24.1.0")
    implementation("net.kyori", "adventure-api", "4.17.0")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.11.0")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine")
}

java{
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_11
}

publishData{
    useEldoNexusRepos()
    publishTask("jar")
    publishTask("sourcesJar")
    publishTask("javadocJar")
}

publishing {
    publications.create<MavenPublication>("maven") {
        publishData.configurePublication(this)
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }

            name = "EldoNexus"
            url = uri(publishData.getRepository())
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava{
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
