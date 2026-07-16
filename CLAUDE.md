# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Hytp is a single-module Android app written in Kotlin with Jetpack Compose (Material 3). It is currently a fresh starter scaffold: the only screen is `MainActivity` rendering a `Greeting` composable. Package/namespace is `com.example.hytp`.

## Commands

Use the Gradle wrapper (`./gradlew` on this bash shell, `gradlew.bat` from cmd).

- Build debug APK: `./gradlew assembleDebug`
- Build everything + run all checks: `./gradlew build`
- Unit tests (JVM, `app/src/test`): `./gradlew testDebugUnitTest`
- Single unit test: `./gradlew testDebugUnitTest --tests "com.example.hytp.ExampleUnitTest.addition_isCorrect"`
- Instrumented tests (needs device/emulator, `app/src/androidTest`): `./gradlew connectedDebugAndroidTest`
- Install on connected device: `./gradlew installDebug`
- Lint: `./gradlew lint` (report at `app/build/reports/lint-results-debug.html`)
- Clean: `./gradlew clean`

## Architecture & conventions

- **Dependencies are managed through the version catalog** at `gradle/libs.versions.toml`. Add/upgrade libraries and plugins there (referenced as `libs.*` in `app/build.gradle.kts`), not inline in build scripts.
- **Compose BOM** (`androidx-compose-bom`) governs all Compose library versions — do not pin individual Compose artifact versions; let the BOM resolve them.
- **UI theming** lives in `app/src/main/java/com/example/hytp/ui/theme/` (`Theme.kt`, `Color.kt`, `Type.kt`). `HytpTheme` enables Android 12+ dynamic color by default and falls back to the hardcoded Purple/Pink schemes otherwise. Wrap new screens in `HytpTheme`.
- **UI is 100% Compose.** There are no XML layouts or Fragments; `MainActivity` uses `setContent { }` with `enableEdgeToEdge()`. Build UI with composables and `@Preview` functions.

## Build environment notes

- Maven repositories are routed through **Aliyun mirrors** (see `settings.gradle.kts` and `pluginManagement`) for faster access in CN networks. Keep new repository declarations consistent with this setup; `RepositoriesMode.FAIL_ON_PROJECT_REPOS` forbids declaring repos in module build files.
- Gradle **configuration cache is enabled** (`gradle.properties`). If a build task fails with a configuration-cache error, it usually means a task isn't cache-compatible — report the specific task rather than disabling the cache globally.
- Targets `compileSdk`/`targetSdk` 36 (minor API 1), `minSdk` 24, Java 11, Kotlin 2.2.10 (official code style), AGP 9.3.0.
- Release build type currently has R8/optimization **disabled** (`optimization { enable = false }`).
