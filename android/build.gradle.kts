plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
}

android {

    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.3.1")
}