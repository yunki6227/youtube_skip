# YouTube Skip

YouTube Skip is an Android-only Kotlin app intended to activate the visible YouTube "Skip ad" accessibility control through Android Accessibility APIs.

## Current Status

- Minimal Android project created.
- Jetpack Compose UI is present.
- Accessibility service registration is present.
- Debug-only emulator event logging is present for supported YouTube accessibility events.
- Debug-only bounded node inspection is present for supported YouTube accessibility events.
- Debug-only skip-candidate ancestor diagnostics are present to identify the likely action target.
- First-pass semantic skip-button detection and `ACTION_CLICK` execution are implemented for emulator validation.
- Automatic skip is controlled by a local in-app switch and defaults to enabled for development builds.
- The UI shows successful skip count, last successful skip time, and the last click result.
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

## Logging

In debug builds, the accessibility service logs compact skip behavior to Android Studio Logcat. Filter Logcat with:

```text
tag:YouTubeSkip
```

Default logs include `skipDetected`, `skipClick attempted`, `skipClick result=...`, `skipVerification result=...`, and compact failure results. General accessibility-event logs, full per-node logging, and scan summaries are suppressed by default.

Deep Stage 4/4.5 diagnostics remain in the code behind the debug-only constant `DETAILED_ACCESSIBILITY_DIAGNOSTICS = false`. Changing that constant to `true` in a debug build restores bounded node scans and candidate ancestry diagnostics. Detailed diagnostics never run in release builds.

When detailed diagnostics are enabled, node inspection is in-memory and bounded to 200 visited nodes and depth 20 per scan. Full scans are throttled to at most once every 1000 ms. Logged node fields may include depth, normalized text, normalized content description, class name, view ID, clickable/enabled flags, and screen bounds. Text and content descriptions are whitespace-normalized and truncated to 80 characters. Editable text fields are omitted. Logs are not persisted or transmitted.

Stage 4.5 adds diagnostic-only logs for skip-related candidate labels. An emulator observation showed the visible skip control exposed as a non-clickable `contentDescription="Skip ad"` node and a non-clickable `text="Skip"` child, with a nearby clickable `android.widget.FrameLayout` ancestor in the same screen region. This suggests the visible label node may not be the action target, and a clickable ancestor may be the node that eventually needs activation.

Candidate diagnostics log only when a skip-related node is encountered. They include the matched node, up to six ancestors, supported accessibility action IDs/names, click-target classification, and a diagnostic recommendation. Identical candidate diagnostics are suppressed in memory for two seconds.

## Automatic Skip Behavior

Stage 5 implements the first production click path. Production detection only triggers from exact, normalized semantic labels:

- `contentDescription == "Skip ad"`
- `contentDescription == "Skip ads"`
- `text == "Skip ad"`
- `text == "Skip ads"`

Production clicking does not trigger from plain `text == "Skip"` or visible-symbol variants alone. The emulator-confirmed hierarchy is a clickable, enabled, visible `android.widget.FrameLayout` with `ACTION_CLICK`, containing a non-clickable `contentDescription="Skip ad"` node and a non-clickable `text="Skip"` child. The app resolves the nearest enabled, visible candidate or ancestor that explicitly supports `AccessibilityNodeInfo.ACTION_CLICK`, then calls `performAction(AccessibilityNodeInfo.ACTION_CLICK)`.

No fixed-coordinate clicking, gesture fallback, OCR, screenshots, networking, analytics, or persistent accessibility data are used. A successful click starts a 3-second in-memory cooldown. After a successful click, the service schedules a one-time verification about 600 ms later and logs whether the semantic candidate disappeared, remained present, or the root was unavailable. `performAction == true` means Android accepted the action; candidate disappearance plus the visible video transition provides stronger emulator confirmation.

The `Automatic skip` switch is stored in local `SharedPreferences`, defaults to enabled, and affects the next YouTube accessibility event without restarting the app or service. The app also stores lightweight local statistics: successful skip count, last successful skip timestamp, and last click result. It stores no ad text, node content, bounds, or screen data.

Failure cases are logged compactly and reflected as the last click result where appropriate: action returned false, no valid click target, target unavailable, or exception.

Korean labels, Samsung-specific behavior, and gesture fallback are not part of the current scope.

Useful Logcat search terms:

```text
nodeScan
Skip
skip
clickable=true
skipCandidate
skipAncestor
skipTarget
skipDetected
skipClick
skipVerification
```

The debug scanner no longer emits general node snapshots during normal automatic execution, but skip-candidate diagnostics remain available in debug logs.

## Emulator Verification

1. Install the latest debug APK.
2. Confirm the accessibility service is enabled.
3. Confirm `Automatic skip` is on in the app.
4. Open Logcat and filter with `tag:YouTubeSkip`.
5. Open YouTube and wait for a skippable ad.
6. Do not manually press Skip.
7. Confirm logs show `skipDetected`, `skipClick attempted`, and `skipClick result=success`.
8. Confirm the advertisement visibly ends and the intended video resumes.
9. Confirm a later log shows `skipVerification result=candidate_disappeared`.
10. Return to the app and confirm the successful skip count, last successful skip time, and last click result update.
11. Turn `Automatic skip` off, return to YouTube, and confirm no detection or click logs are emitted for later events.
12. Confirm unrelated apps receive no click attempts.

## Roadmap

1. Project bootstrap.
2. Accessibility service registration.
3. Emulator event logging.
4. Accessibility node-tree inspection.
5. Semantic skip detection and accessibility click execution.
6. Gesture fallback evaluation, only if needed.
7. Debounce and successful-click cooldown refinement.
8. Compose status/settings UI.
9. Emulator integration testing.
10. Physical Android device testing.
