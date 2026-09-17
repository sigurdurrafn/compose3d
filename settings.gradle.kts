pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

// The Android app and common's android target depend on AGP and androidx,
// which are published only to Google's Maven repository. That repository is
// unreachable in some sandboxed environments, so both can be opted out with
// -PskipAndroid=true; Android is included by default.
//
// common's android target lives in a second build file,
// common/build.android.gradle.kts, that is swapped in only when Android is
// enabled. A single build.gradle.kts with an `if (!skipAndroid)` around the
// Android plugin application was tried first, but Gradle's Kotlin DSL resolves
// the `android { ... }` typed extension accessor at script-compile time, so it
// still needed the AGP plugin's classes on the script classpath even on the
// branch that never runs. Swapping the whole file sidesteps that: the
// skipped-Android file never mentions AGP types at all.
val skipAndroid = gradle.startParameter.projectProperties["skipAndroid"] == "true"

include(
    ":common",
    ":desktop",
    ":engine",
)

if (!skipAndroid) {
    include(":android")
    project(":common").buildFileName = "build.android.gradle.kts"
}
