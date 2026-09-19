@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    jvmToolchain(17)

    jvm()
    iosArm64()
    iosSimulatorArm64()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    android {
        namespace = "compose3d.common"
        compileSdk = 37
        // See android/build.gradle.kts: drawVertices needs API 29.
        minSdk = 29
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                api(compose.animation)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                api(project(":engine"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        // The Skiko `drawTriangles` actual (org.jetbrains.skia.Canvas) is the
        // same call on every target whose Compose UI implementation is
        // backed by Skia: desktop (jvm), iOS and wasmJs. Android's Compose UI
        // is backed by the platform's own android.graphics.Canvas instead, so
        // it gets its own actual (src/androidMain/kotlin/DrawTriangles.android.kt).
        val skikoMain by creating {
            dependsOn(commonMain)
        }
        val jvmMain by getting { dependsOn(skikoMain) }
        val iosArm64Main by getting { dependsOn(skikoMain) }
        val iosSimulatorArm64Main by getting { dependsOn(skikoMain) }
        val wasmJsMain by getting { dependsOn(skikoMain) }
    }
}

compose.resources {
    // Pinned explicitly rather than relying on the `{group}.{module}.generated.resources`
    // default, since this project sets no group id.
    packageOfResClass = "compose3d.resources"
}
