@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
}

version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {

    jvm()

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
