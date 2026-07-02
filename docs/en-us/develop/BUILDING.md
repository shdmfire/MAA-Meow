# Build Guide

## Prerequisites

- Install [Eclipse Temurin JDK 25](https://adoptium.net/temurin/releases?version=25)

- Install [Android Studio](https://developer.android.com/studio)

- Download the MAA Core prebuilt artifacts (.so libraries + resource files)

  ```bash
  python scripts/setup_maa_core.py
  ```

## Build Steps

- Open this folder in Android Studio. Under Settings - Build, Execution, Deployment - Build Tools - Gradle - Gradle Projects - Gradle JDK, select the temurin-25 you installed earlier.

- Run "Sync Project with Gradle Files". Android Studio will install the remaining dependencies automatically. Once finished, run the "Assemble app" Run Configuration to build the APK.
