# YouTube Skip Project Instructions

## Project Goal

Build a small Android app that detects a visible and user-actionable YouTube "Skip ad" control through Android Accessibility APIs and activates it.

The initial target is local development and emulator testing.

## Technical Stack

- Kotlin
- Jetpack Compose
- Gradle Kotlin DSL
- Material 3
- Minimum SDK 26
- Compile SDK 36
- Target SDK 36
- Application ID `com.yunki.youtubeskip`

## Architecture

Use these package boundaries as functionality is added:

- `accessibility`
  - Android AccessibilityService integration
  - accessibility-node traversal
  - click execution
- `detection`
  - text normalization
  - matching rules
  - detection results
- `settings`
  - local preferences
- `ui`
  - Compose screens and components
- `util`
  - logging and small shared helpers

Keep Android framework entry points thin.

Do not place node traversal, matching, and clicking logic directly inside `YouTubeAccessibilityService`.

Do not create empty abstraction classes before they are needed.

## Behavioral Constraints

- Process only `com.google.android.youtube`
- Initially recognize:
  - `Skip`
  - `Skip >|`
  - `Skip ad`
  - `Skip ads`
- `>|` is a visible symbol and may not be exposed literally in the accessibility tree
- Future detection must inspect both `AccessibilityNodeInfo.text` and `AccessibilityNodeInfo.contentDescription`
- Do not assume the visible button label exactly matches the accessibility label
- Matching must be case-insensitive and whitespace-normalized
- Prefer accessibility-node `ACTION_CLICK`
- If the matched node is not clickable, search for a clickable ancestor
- Gesture dispatch may exist only as an isolated fallback
- Never use hardcoded or fixed screen coordinates
- Debounce accessibility events
- Add a cooldown after successful clicks
- Handle null and stale accessibility nodes safely

## Prohibited Functionality

Do not add:

- OCR
- screen capture
- media projection
- continuous screenshots
- root access
- fixed-coordinate clicking
- networking
- analytics
- advertisements
- remote services
- user accounts
- `INTERNET` permission
- unnecessary Android permissions
- unnecessary dependency-injection frameworks

## Privacy

- Do not collect accessibility content
- Do not transmit accessibility content
- Do not store YouTube screen text
- Debug logs must be local and disabled or reduced in release builds

## Development Process

For every implementation task:

1. Inspect the repository and `AGENTS.md`
2. Explain the planned change briefly
3. Make the smallest complete change
4. Avoid unrelated redesigns
5. Run relevant tests
6. Run lint
7. Build the debug APK
8. Report changed files
9. Report exact validation commands and results
10. Clearly identify unresolved limitations

Keep the repository compiling after every task.
