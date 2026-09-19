plugins {
    alias(libs.plugins.android.application)
    // AGP 9 compiles Kotlin itself; org.jetbrains.kotlin.android is no longer applied.
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.myapplication"
    compileSdk = 37
    defaultConfig {
        // Canvas.drawVertices is only honoured on a hardware-accelerated
        // canvas from API 29, and Compose always draws into one, so below 29
        // the renderer would silently draw nothing.
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        // Built-in Kotlin takes its jvmTarget from targetCompatibility.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":common"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
}
