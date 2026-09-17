plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    // AGP is requested directly (not aliased here with apply false) by
    // android/build.gradle.kts and common/build.gradle.kts, so that resolving
    // it is skipped entirely along with those when -PskipAndroid=true.
}
