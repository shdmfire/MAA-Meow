plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aliothmoon.maameow.automation.app"
    compileSdk = 37
    defaultConfig { minSdk = 28 }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":automation:android-ipc"))
    implementation(libs.shizuku.api)
    implementation(libs.timber)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    testImplementation(libs.junit)
}
