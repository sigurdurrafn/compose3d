plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    // AGP is requested directly by android/build.gradle.kts and
    // common/build.gradle.kts instead of pre-declared here with apply false;
    // pre-declaring it here would gain nothing (each of those files' own
    // plugins {} block already carries the version through the alias) and
    // would make Gradle try to resolve it as soon as the root project
    // configures, for every invocation.
}
