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
- Logs contain event type names and package name only, plus event timestamp.
- Logcat can be filtered with `tag:YouTubeSkip`.
- Node inspection, skip-button detection, and click execution are not implemented in this stage.

## 4. Accessibility Node-Tree Inspection

Acceptance criteria:

- Node traversal handles null and stale nodes safely.
- Traversal is isolated outside the service.
- Inspection can identify visible candidate text nodes from YouTube.
- Debug-only inspection logs bounded node metadata with `tag:YouTubeSkip`.
- Scans visit at most 200 nodes and depth 20.
- Full node scans are throttled to at most once every 1000 ms.
- Logs are not persisted or transmitted.
- Clicking and skip-button detection are not implemented in this stage.

## 4.5. Skip Candidate Action-Target Diagnostics

Acceptance criteria:

- Observed YouTube hierarchy is documented: a visible skip control may expose a non-clickable `contentDescription="Skip ad"` node, a non-clickable `text="Skip"` child, and a clickable ancestor in the same region.
- Exact skip-related diagnostic labels are matched without loose substring matching.
- Candidate diagnostics include the matched node and up to six ancestors.
- Diagnostics include supported accessibility action IDs and readable action names.
- Diagnostics classify nodes as direct click action, clickable flag only, non-clickable, disabled, or not visible.
- Diagnostics recommend a target for investigation only; no automatic click is performed.
- Repeated identical candidate diagnostics are suppressed in memory for a short window.
- Logcat can be searched with `skipCandidate`, `skipAncestor`, and `skipTarget`.
- Final skip-button detection and click execution are not implemented in this stage.

## 5. Semantic Detection and Accessibility Click Execution

Acceptance criteria:

- Production detection prioritizes semantic labels `Skip ad` and `Skip ads`.
- Matching is exact, case-insensitive, and whitespace-normalized.
- Plain `text="Skip"` and visible-symbol variants remain diagnostic-only and do not independently trigger clicking.
- The confirmed hierarchy is used: a semantic `Skip ad` candidate can resolve to a nearest clickable `FrameLayout` ancestor with `ACTION_CLICK`.
- The click target resolver walks up through at most four ancestors.
- Click execution invokes `performAction(AccessibilityNodeInfo.ACTION_CLICK)` once on a resolved enabled, visible, explicit-click target.
- Fixed coordinates, OCR, screenshots, networking, analytics, and gesture tapping are not used.
- A 350 ms event debounce and 3-second successful-click cooldown are present.
- Post-click verification runs once about 600 ms after success and does not click again.
- A local `Automatic skip enabled` switch defaults to enabled.
- Unit tests cover matching, target resolution policy, deduplication, debounce, cooldown, click-result mapping, and verification classification.

## 6. Gesture Fallback Evaluation

Acceptance criteria:

- Consider gesture dispatch only if emulator and device validation show that accessibility `ACTION_CLICK` is insufficient.
- Gesture fallback, if added, remains isolated and never uses hardcoded fixed coordinates.
- OCR and screenshots remain excluded.

## 7. Debounce and Successful-Click Cooldown Refinement

Acceptance criteria:

- Tune debounce and cooldown using emulator and physical-device observations.
- Successful clicks start a cooldown.
- Cooldown behavior remains covered by tests where practical.

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
