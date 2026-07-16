plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.aliothmoon.maameow.controller.maa.engine"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
        // setup_maa_core.py deploy 时写入 .maaversion；缺失时为空串
        val maaCoreVersion = rootProject.file(".maaversion")
            .takeIf { it.isFile }?.readText()?.trim().orEmpty()
        buildConfigField("String", "MAA_CORE_VERSION", "\"$maaCoreVersion\"")
        consumerProguardFiles("proguard-rules.pro")
    }

    buildFeatures {
        buildConfig = true
        aidl = true
    }

    // 导出 AIDL 接口，使 app 模块可以 import 这些类型
    requireNotNull(aidlPackagedList).add(
        "com/aliothmoon/maameow/controller/maa/engine/MaaCoreService.aidl"
    )
    requireNotNull(aidlPackagedList).add(
        "com/aliothmoon/maameow/controller/maa/engine/MaaCoreCallback.aidl"
    )

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":controller:maa-contract"))
    implementation(project(":automation:api"))
    implementation(project(":automation:runtime-remote"))
    implementation(project(":automation:android-ipc"))
    implementation(libs.jna) { artifact { type = "aar" } }
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.fastjson2)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
}
