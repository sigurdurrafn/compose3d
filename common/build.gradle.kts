@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")
@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    jvmToolchain(17)

    // The Skiko `drawTriangles` actual (org.jetbrains.skia.Canvas) is the same
    // call on every target whose Compose UI is backed by Skia: desktop (jvm),
    // iOS and wasmJs. Android's Compose UI is backed by the platform's own
    // android.graphics.Canvas instead, so it stays out of this group and gets
    // its own actual in src/androidMain.
    //
    // Declaring the shared source set as a hierarchy group rather than wiring
    // dependsOn by hand keeps Kotlin's default template in play; explicit
    // dependsOn edges switch it off for the whole project.
    applyDefaultHierarchyTemplate {
        common {
            group("skiko") {
                withJvm()
                withIos()
                withWasmJs()
            }
        }
    }

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
    }
}

compose.resources {
    // Pinned explicitly rather than relying on the `{group}.{module}.generated.resources`
    // default, since this project sets no group id.
    packageOfResClass = "compose3d.resources"
}
