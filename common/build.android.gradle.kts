@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

// Used instead of build.gradle.kts when Android is not skipped (see
// settings.gradle.kts). Unverified in this sandbox: AGP and androidx are
// published only to Google's Maven repository, which is unreachable here.
// Keep targets and dependencies in sync with build.gradle.kts when editing.

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    // No separate compose-compiler plugin: see build.gradle.kts.
    alias(libs.plugins.android.library)
}

// See engine/build.gradle.kts for why native targets are opt-out.
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
    androidTarget()

    applyDefaultHierarchyTemplate()

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
        // backed by Skia: desktop (jvm), iOS and wasmJs.
        val skikoMain by creating {
            dependsOn(commonMain)
        }
        val jvmMain by getting { dependsOn(skikoMain) }
        if (!skipNative) {
            val iosMain by getting { dependsOn(skikoMain) }
        }
        val wasmJsMain by getting { dependsOn(skikoMain) }

        // Android's Compose UI is backed by the platform's own
        // android.graphics.Canvas, not Skiko, so it gets its own actual
        // (src/androidMain/kotlin/DrawTriangles.android.kt).
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.appcompat)
            }
        }
    }
}

android {
    namespace = "compose3d.common"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
