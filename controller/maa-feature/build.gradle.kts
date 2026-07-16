plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aliothmoon.maameow.controller.maa.feature"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

// Apply asset manifest generation script
apply(from = "asset-manifest.gradle.kts")
