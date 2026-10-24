plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.publishdata)
    alias(libs.plugins.spotless)
}

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
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            url.set("https://github.com/eldoriarpg/messageblocker")
            developers {
                developer {
                    name.set("Nora Fülling")
                    organization.set("EldoriaRPG")
                    organizationUrl.set("https://github.com/eldoriarpg")
                }
            }
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
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
