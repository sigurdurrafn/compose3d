plugins {
    kotlin("multiplatform")
}

version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {

//    android()
//    jvm("desktop")
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

//android {
//
//    compileSdk = 30
//    defaultConfig {
//        minSdk = 21
//        targetSdk = 30
//    }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//
////    sourceSets {
////        named("main") {
////            manifest.srcFile("src/androidMain/AndroidManifest.xml")
////            res.srcDirs("src/androidMain/res")
////        }
////    }
//}