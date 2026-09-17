plugins {
    // Every plugin is declared here with apply false so that all of them,
    // AGP included, load in the root classloader. The Kotlin plugin's Android
    // integration references AGP classes; with AGP applied only in the
    // subprojects it fails with NoClassDefFoundError: BaseExtension.
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}
