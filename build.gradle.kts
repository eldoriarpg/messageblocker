plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.publishdata)
    alias(libs.plugins.spotless)
}

description = "Holds back chat for players while a plugin shows them a chat menu."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.protocollib)
    compileOnly(libs.annotations)

    testImplementation(libs.paper)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
    withJavadocJar()
}

publishData {
    eldoNexus(dev = false)
    channels {
        clear()
        release("https://eldonexus.de/repository/maven-releases/")
        snapshot("https://eldonexus.de/repository/maven-snapshots/")
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            url = "https://github.com/eldoriarpg/messageblocker"
            inceptionYear = "2021"
            licenses {
                license {
                    name = "LGPL-3.0-or-later"
                    url = "https://www.gnu.org/licenses/lgpl-3.0.html"
                }
            }
            developers {
                developer {
                    id = "rainbowdashlabs"
                    name = "Nora Fülling"
                    email = "mail@chojo.dev"
                    url = "https://github.com/rainbowdashlabs"
                    organization = "Eldoria"
                    organizationUrl = "https://github.com/eldoriarpg"
                }
            }
            scm {
                url = "https://github.com/eldoriarpg/messageblocker"
                connection = "scm:git:git://github.com/eldoriarpg/messageblocker.git"
                developerConnection = "scm:git:ssh://github.com/eldoriarpg/messageblocker.git"
            }
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 21
        options.encoding = "UTF-8"
    }

    compileJava {
        options.compilerArgs.addAll(listOf("--add-reads", "de.eldoria.messageblocker=ALL-UNNAMED"))
    }

    javadoc {
        (options as StandardJavadocDocletOptions).addStringOption("-add-reads", "de.eldoria.messageblocker=ALL-UNNAMED")
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

spotless {
    java {
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        palantirJavaFormat(libs.versions.palantir.get()).formatJavadoc(false)
        removeUnusedImports()
        importOrder("", "java", "javax", "\\#")
        trimTrailingWhitespace()
        endWithNewline()
        encoding("UTF-8")
    }
}
