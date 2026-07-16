plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.aliothmoon.maameow.automation.ipc"
    compileSdk = 37

    defaultConfig { minSdk = 28 }
    buildFeatures { aidl = true }
    requireNotNull(aidlPackagedList).add(
        "com/aliothmoon/maameow/automation/ipc/ITouchEventCallback.aidl",
    )
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
