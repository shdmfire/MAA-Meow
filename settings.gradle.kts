pluginManagement {
    repositories {
        mavenLocal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
    }
}

rootProject.name = "MaaMeow"
include(":app")
include(":automation:api")
include(":automation:android-ipc")
include(":automation:runtime-app")
include(":automation:runtime-remote")
include(":hidden-api")
include(":annotation-api")
include(":ksp-processor")
include(":controller:maa-contract")
include(":controller:maa-engine")
include(":controller:maa-feature")
 
