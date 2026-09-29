plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

// Demos for the teaching posts. Only the targets that show them: the browser
// bundle, and the JVM for tests and desktop previews.
kotlin {
    jvmToolchain(17)

    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":common"))
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
