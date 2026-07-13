# YouTube Skip

YouTube Skip is an Android-only Kotlin app intended to explore automatic activation of the visible YouTube "Skip ad" accessibility control. The current project registers an accessibility service shell, but it does not skip ads yet.

## Current Status

- Minimal Android project created.
- Jetpack Compose UI is present.
- Accessibility service registration is present.
- Debug-only emulator event logging is present for supported YouTube accessibility events.
- Skip-button detection and click behavior are not implemented yet.
- Accessibility node inspection is not implemented yet.
- The initial development target is a Pixel 8 Android emulator.
- Physical-device testing will eventually be required because accessibility behavior can differ from emulator behavior.

## Technology Stack

- Kotlin
- Jetpack Compose
- Material 3
- Gradle Kotlin DSL
- Minimum SDK 26
- Compile SDK 36
- Target SDK 36
- Application ID `com.yunki.youtubeskip`

## Emulator-First Setup

1. Install Android Studio with Android SDK API 36.
2. Create or start a Pixel 8 emulator.
3. Open this repository in Android Studio.
4. Let Android Studio sync the Gradle project.

## Build

From the repository root:

```sh
./gradlew assembleDebug
```

The debug APK is generated under:

```text
app/build/outputs/apk/debug/
```

## Tests

Run local unit tests:

```sh
./gradlew test
```

## Lint

Run Android lint:

```sh
./gradlew lint
```

## Debug Event Logging

In debug builds, the accessibility service logs safe YouTube event metadata to Android Studio Logcat. Filter Logcat with:

```text
tag:YouTubeSkip
```

Logs contain only the supported event type name, the package name `com.google.android.youtube`, and the event timestamp. They do not include node text, content descriptions, view IDs, class names, window content, user-entered text, URLs, or account information.

Node inspection, skip-button detection, and clicking are not implemented yet.

## Roadmap

1. Project bootstrap.
2. Accessibility service registration.
3. Emulator event logging.
4. Accessibility node-tree inspection.
5. Text normalization and matching.
6. Node click execution.
7. Debounce and successful-click cooldown.
8. Compose status/settings UI.
9. Emulator integration testing.
10. Physical Android device testing.
