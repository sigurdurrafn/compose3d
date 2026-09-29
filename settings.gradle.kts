pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    // Provisions the JDK requested by jvmToolchain(17) when none is installed.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(
    ":common",
    ":android",
    ":desktop",
    ":engine",
    ":lessons",
    ":web",
)
