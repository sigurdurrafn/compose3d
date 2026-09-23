plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "compose3d.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.components.resources)
            }
        }
    }
}

// Copies the production bundle into a host site's static files, so the site
// can mount demos with the exported `mountDemo`. Defaults to the Kobweb site
// checked out next to this repo; pass -PsiteDir=... to point elsewhere.
tasks.register<Sync>("syncToSite") {
    group = "distribution"
    description = "Copies the wasm bundle into the website's public/compose3d directory."
    val siteDir = providers.gradleProperty("siteDir")
        .orElse(rootDir.resolve("../gunnarss/site").path)
    from(tasks.named("wasmJsBrowserDistribution"))
    exclude("index.html", "embed.html", "*.map")
    into(siteDir.map { "$it/src/jsMain/resources/public/compose3d" })
}
