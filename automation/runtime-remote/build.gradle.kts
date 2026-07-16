plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aliothmoon.maameow.automation.remote"
    compileSdk = 37

    defaultConfig { minSdk = 28 }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":automation:api"))
    implementation(project(":automation:android-ipc"))
    compileOnly(project(":hidden-api"))
    compileOnly("androidx.annotation:annotation:1.10.0")
    implementation(libs.shizuku.api)
    implementation(libs.timber)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
