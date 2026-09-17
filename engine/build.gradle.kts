@file:Suppress("UNUSED_VARIABLE")

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

version = "1.0.0"

repositories {
    mavenCentral()
}

// Kotlin 1.9.20+ eagerly downloads the Kotlin/Native compiler as soon as a
// native target (iosArm64, iosSimulatorArm64) is declared, during ordinary
// project configuration, for any task including an unrelated JVM one. That
// download is unreachable in some sandboxed environments, so the targets are
// opt-out with -PskipNative=true; they are declared by default. iOS cannot be
// linked from a non-macOS host regardless (see the README).
val skipNative = gradle.startParameter.projectProperties["skipNative"] == "true"

kotlin {
    jvmToolchain(17)

    jvm()
    if (!skipNative) {
        iosArm64()
        iosSimulatorArm64()
    }
    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
