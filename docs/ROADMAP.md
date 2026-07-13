# Roadmap

## 1. Project Bootstrap

Acceptance criteria:

- Android project uses Gradle Kotlin DSL.
- App module builds with Kotlin and Jetpack Compose.
- Minimum SDK is 26.
- Compile SDK is 36.
- Target SDK is 36.
- Minimal Compose UI is present.
- `./gradlew test`, `./gradlew lint`, and `./gradlew assembleDebug` pass.

## 2. Accessibility Service Registration

Acceptance criteria:

- Accessibility service is declared in the manifest.
- Service configuration targets only `com.google.android.youtube`.
- No unnecessary permissions are added.
- UI can open Android Accessibility Settings.
- Skip-button detection, node traversal, and click execution are not implemented in this stage.

## 3. Emulator Event Logging

Acceptance criteria:

- Local debug-only logs show relevant YouTube accessibility event types.
- Release builds reduce or disable debug logging.
- Logs do not collect or persist accessibility content.

## 4. Accessibility Node-Tree Inspection

Acceptance criteria:

- Node traversal handles null and stale nodes safely.
- Traversal is isolated outside the service.
- Inspection can identify visible candidate text nodes from YouTube.

## 5. Pure Text Normalization and Matching

Acceptance criteria:

- Matching is case-insensitive.
- Matching is whitespace-normalized.
- Initial possible labels `Skip`, `Skip >|`, `Skip ad`, and `Skip ads` are recognized where exposed by accessibility APIs.
- Matching inspects both `AccessibilityNodeInfo.text` and `AccessibilityNodeInfo.contentDescription`.
- Matching does not assume the visible label exactly matches the accessibility label.
- Matching logic is unit-tested without Android framework dependencies where practical.

## 6. Node Click Execution

Acceptance criteria:

- Click execution prefers node `ACTION_CLICK`.
- If the matched node is not clickable, a clickable ancestor is used.
- Fixed coordinates are not used.
- Gesture dispatch, if needed, is isolated as a fallback.

## 7. Debounce and Successful-Click Cooldown

Acceptance criteria:

- Event bursts are debounced.
- Successful clicks start a cooldown.
- Cooldown behavior is covered by tests where practical.

## 8. Compose Status/Settings UI

Acceptance criteria:

- UI reports service configuration status.
- UI can open Accessibility Settings.
- Any settings are local-only.
- UI does not add networking, analytics, accounts, or ads.

## 9. Emulator Integration Testing

Acceptance criteria:

- Pixel 8 emulator can install and launch the app.
- Accessibility service can be enabled manually.
- YouTube events are observed without crashes.
- Basic skip-button detection path is exercised on emulator.

## 10. Physical Android Device Testing

Acceptance criteria:

- App is tested on at least one physical Android device.
- Accessibility behavior differences from emulator are documented.
- Any device-specific fixes preserve the no-OCR, no-screenshot, no-fixed-coordinate constraints.
