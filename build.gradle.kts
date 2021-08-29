buildscript {

    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.0.0-alpha1")
        classpath("com.android.tools.build:gradle:7.1.0-alpha10")
        classpath(kotlin("gradle-plugin", version = "1.5.21"))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
