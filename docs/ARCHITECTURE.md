# Architecture

## Current Package Structure

```text
app/src/main/java/com/yunki/youtubeskip/
  MainActivity.kt
  accessibility/
    AccessibilityEventLogPolicy.kt
    AccessibilityNodeScanner.kt
    AccessibilityNodeScanThrottle.kt
    AccessibilityNodeSnapshot.kt
    AccessibilityServiceStatus.kt
    YouTubeAccessibilityService.kt
  ui/
    HomeScreen.kt
  util/
    AppLogger.kt
```

The current app registers a minimal accessibility service shell, provides a Compose button that opens Android Accessibility Settings, emits debug-only safe Logcat messages for supported YouTube accessibility events, and performs bounded debug-only node inspection. Skip-button detection and click behavior are not implemented yet.

## Planned Package Structure

```text
app/src/main/java/com/yunki/youtubeskip/
  MainActivity.kt
  accessibility/
    YouTubeAccessibilityService.kt
    AccessibilityNodeScanner.kt
    NodeClickExecutor.kt
  detection/
    SkipButtonDetector.kt
    SkipButtonMatchers.kt
    DetectionResult.kt
  settings/
    AppPreferences.kt
  ui/
    HomeScreen.kt
  util/
    AppLogger.kt
```

Planned classes should be added only when they have real behavior.

## Planned Responsibilities

### `YouTubeAccessibilityService`

Current Android framework entry point for accessibility events. It remains thin: ignore null and non-YouTube events, handle only window content/state changes, emit safe debug-only event logs through `AppLogger`, and delegate bounded debug node inspection to `AccessibilityNodeScanner`. Future detection and click execution should still be delegated to dedicated collaborators.

### `AccessibilityEventLogPolicy`

Current helper for readable event type names and log-only throttling. It only supports `WINDOW_CONTENT_CHANGED` and `WINDOW_STATE_CHANGED`; unknown event types are ignored.

### `AccessibilityNodeScanner`

Current debug-only component responsible for safe, iterative traversal of visible accessibility node trees from YouTube. It visits at most 200 nodes, scans to depth 20, skips editable/input-looking nodes for logging, keeps only immutable snapshots, and handles stale nodes defensively. It does not click, mutate, persist, or transmit node data.

### `AccessibilityNodeSnapshot`

Current immutable debug data model for node metadata. Text and content descriptions are trimmed, whitespace-normalized, and truncated to 80 characters before logging.

### `AccessibilityNodeScanThrottle`

Current in-memory logging throttle for full node scans. It allows at most one scan every 1000 ms and does not suppress future detection logic.

### `SkipButtonDetector`

Planned component that receives candidate nodes and returns detection results for visible, user-actionable skip controls.

### `SkipButtonMatchers`

Planned pure matching helpers for case-insensitive, whitespace-normalized text matching. Initial possible target labels are `Skip`, `Skip >|`, `Skip ad`, and `Skip ads`. The visible `>|` symbol may not be exposed literally in the accessibility tree, so future detection must inspect both `AccessibilityNodeInfo.text` and `AccessibilityNodeInfo.contentDescription` and must not assume the visible label exactly matches the accessibility label.

### `NodeClickExecutor`

Planned component that prefers accessibility-node `ACTION_CLICK`, then searches for a clickable ancestor when the matched node itself is not clickable. Gesture dispatch may exist only as an isolated fallback.

### `AppPreferences`

Planned local preferences wrapper for future user-facing settings. It must not store YouTube screen text or accessibility content.

### Compose UI

Current Compose UI shows the app status, target app, and a short Logcat note. Future UI can expose local status/settings without analytics, network calls, accounts, or ads.

## Intended Event Flow

1. Android delivers an accessibility event.
2. `YouTubeAccessibilityService` ignores events outside `com.google.android.youtube`.
3. The service logs safe debug-only event metadata for supported event types.
4. During Stage 4 debug builds, `AccessibilityNodeScanner` gathers bounded immutable snapshots for inspection only.
5. Future debounce and cooldown rules will be added before automatic detection/clicking.
6. Future `SkipButtonDetector` will check normalized text and content descriptions using `SkipButtonMatchers`.
7. Future `NodeClickExecutor` will activate the best matched actionable node.
8. The service records only local operational state needed for throttling, cooldown, or UI status.

## Intended Click Fallback Order

1. Click the matched node with `ACTION_CLICK` when it is clickable.
2. Search upward for a clickable ancestor and invoke `ACTION_CLICK`.
3. Use gesture dispatch only as an isolated fallback if it becomes necessary.

Fixed coordinates are never allowed.

## Exclusions From the First Version

Fixed coordinates are excluded because YouTube layouts vary by device, density, orientation, locale, and app version.

OCR is excluded because the intended implementation should rely on accessibility semantics, not visual scraping.

Screenshots and screen recording are excluded to keep privacy boundaries tight and avoid unnecessary sensitive-data handling.
