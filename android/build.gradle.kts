// Only evaluated when Android is not skipped (see settings.gradle.kts).
// Unverified in this sandbox: AGP and androidx are published only to
// Google's Maven repository, which is unreachable here.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
    // No separate compose-compiler plugin: see the version-pin note in
    // gradle/libs.versions.toml.
}

android {
    compileSdk = 35
    defaultConfig {
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "com.myapplication"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":common"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
}
