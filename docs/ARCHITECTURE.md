# Architecture

## Current Package Structure

```text
app/src/main/java/com/yunki/youtubeskip/
  MainActivity.kt
  accessibility/
    AccessibilityActionNames.kt
    AccessibilityEventLogPolicy.kt
    AccessibilityNodeScanner.kt
    AccessibilityNodeScanThrottle.kt
    AccessibilityNodeSnapshot.kt
    AccessibilityProcessingGuards.kt
    NodeClickExecutor.kt
    NodeClickTargetResolver.kt
    AccessibilityServiceStatus.kt
    SkipCandidateDiagnostic.kt
    YouTubeAccessibilityService.kt
  detection/
    SkipButtonDetector.kt
    SkipButtonLabelMatcher.kt
    SkipCandidateMatcher.kt
  settings/
    AppPreferences.kt
  ui/
    HomeScreen.kt
  util/
    AppLogger.kt
```

The current app registers an accessibility service, provides Compose controls for Android Accessibility Settings and a local automatic-skip toggle, emits debug-only safe Logcat messages for supported YouTube accessibility events, performs bounded debug-only skip diagnostics, and executes the first semantic `ACTION_CLICK` skip path for emulator validation.

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

Current Android framework entry point for accessibility events. It remains thin: ignore null and non-YouTube events, handle only window content/state changes, emit safe debug-only event logs through `AppLogger`, enforce the local automatic-skip preference, apply event debounce and successful-click cooldown, delegate semantic detection to `SkipButtonDetector`, delegate click execution to `NodeClickExecutor`, and schedule one-time post-click verification.

### `AccessibilityEventLogPolicy`

Current helper for readable event type names and log-only throttling. It only supports `WINDOW_CONTENT_CHANGED` and `WINDOW_STATE_CHANGED`; unknown event types are ignored.

### `AccessibilityNodeScanner`

Current debug-only component responsible for safe, iterative traversal of visible accessibility node trees from YouTube. It visits at most 200 nodes, scans to depth 20, skips editable/input-looking nodes for logging, keeps only immutable snapshots, and handles stale nodes defensively. When it encounters a focused skip-related diagnostic candidate, it captures the candidate plus up to six ancestors as immutable data. General node snapshot logging is reduced during normal automatic execution; `skipCandidate`, `skipAncestor`, and `skipTarget` diagnostics remain available. It does not click, mutate, persist, or transmit node data.

### `AccessibilityNodeSnapshot`

Current immutable debug data model for node metadata. Text and content descriptions are trimmed, whitespace-normalized, and truncated to 80 characters before logging.

### `AccessibilityNodeScanThrottle`

Current in-memory logging throttle for full node scans. It allows at most one scan every 1000 ms and does not suppress future detection logic.

### `SkipCandidateMatcher`

Current pure diagnostic matcher for Stage 4.5. It recognizes exact, case-insensitive, whitespace-normalized skip labels without loose substring matching. For diagnostics, `AccessibilityNodeInfo.text` may match `Skip`, `Skip ad`, `Skip ads`, `Skip >|`, `Skip ▶|`, or `Skip ▷|`; `AccessibilityNodeInfo.contentDescription` may match `Skip ad`, `Skip ads`, `Skip >|`, `Skip ▶|`, or `Skip ▷|`. This is not the final production detector.

### `SkipCandidateDiagnostic`

Current immutable diagnostic model for Stage 4.5. It records the matched node, a bounded ancestor chain, supported action IDs/names, visibility/enabled/focusable/clickable flags, and a recommended diagnostic target. Recommendation is log-only: prefer an enabled visible candidate with `ACTION_CLICK`, then the nearest enabled visible ancestor with `ACTION_CLICK`, then the nearest enabled visible clickable ancestor.

### `AccessibilityActionNames`

Current helper that maps known accessibility action IDs, including `ACTION_CLICK`, `ACTION_DISMISS`, and `ACTION_FOCUS`, to readable debug names.

### `SkipButtonDetector`

Current bounded production detector for semantic skip controls. It traverses at most 200 nodes to depth 20, matches exact normalized `Skip ad` / `Skip ads` labels, deduplicates candidates that resolve to the same visible target, and returns one result for the synchronous detection/click operation. It may hold live `AccessibilityNodeInfo` references only in the returned event-local result; it does not store live nodes in fields or preferences.

### `SkipButtonLabelMatcher`

Current pure production matcher. It prioritizes semantic labels in this order: `contentDescription="Skip ad"`, `contentDescription="Skip ads"`, `text="Skip ad"`, then `text="Skip ads"`. Matching is case-insensitive, trim/whitespace-normalized, and exact. Plain `text="Skip"` and visible-symbol variants are diagnostic-only and do not independently trigger production clicking.

### `SkipButtonMatchers`

Planned pure matching helpers for case-insensitive, whitespace-normalized text matching. Initial possible target labels are `Skip`, `Skip >|`, `Skip ad`, and `Skip ads`. The visible `>|` symbol may not be exposed literally in the accessibility tree, so future detection must inspect both `AccessibilityNodeInfo.text` and `AccessibilityNodeInfo.contentDescription` and must not assume the visible label exactly matches the accessibility label.

### `NodeClickTargetResolver`

Current resolver that starts at the semantic candidate and walks up through at most four ancestors. It prefers the first enabled, visible node that explicitly supports `ACTION_CLICK`. If no explicit click action exists, it can identify the nearest enabled, visible clickable node as a diagnostic fallback, but the executor still requires explicit `ACTION_CLICK` before invoking an action.

### `NodeClickExecutor`

Current component that accepts a resolved target, re-checks enabled state, visibility, and explicit `ACTION_CLICK` support, then calls `performAction(AccessibilityNodeInfo.ACTION_CLICK)` once. It returns structured results for success, `false` action return, stale/unavailable target, no longer enabled/visible target, unavailable click action, and runtime exceptions. It does not retry.

### `AccessibilityProcessingGuards`

Current pure guards for a 350 ms event-processing debounce, a 3-second successful-click cooldown, cooldown suppression-log throttling, and post-click verification result classification.

### `AppPreferences`

Current local `SharedPreferences` wrapper for the `Automatic skip enabled` switch. It defaults to enabled and stores only this local boolean; it does not store YouTube screen text or accessibility content.

### Compose UI

Current Compose UI shows the app status, target app, and a short Logcat note. Future UI can expose local status/settings without analytics, network calls, accounts, or ads.

## Intended Event Flow

1. Android delivers an accessibility event.
2. `YouTubeAccessibilityService` ignores events outside `com.google.android.youtube`.
3. The service logs safe debug-only event metadata for supported event types.
4. If automatic skip is disabled locally, detection and clicking stop.
5. A 350 ms debounce suppresses event bursts before detection.
6. A 3-second cooldown suppresses repeated processing after a successful click.
7. The service reads `rootInActiveWindow`.
8. `SkipButtonDetector` searches for one semantic `Skip ad` / `Skip ads` target.
9. `NodeClickTargetResolver` chooses the nearest enabled, visible `ACTION_CLICK` node.
10. `NodeClickExecutor` invokes `performAction(ACTION_CLICK)` once.
11. A successful click schedules a non-blocking verification about 600 ms later.
12. Debug diagnostics log `skipDetected`, `skipClick`, `skipVerification`, and skip-candidate ancestor details.

## Intended Click Fallback Order

1. Use the matched semantic candidate itself if enabled, visible, and explicitly supporting `ACTION_CLICK`.
2. Search upward through at most four ancestors for the nearest enabled, visible node explicitly supporting `ACTION_CLICK`.
3. Record clickable-only fallback information diagnostically if needed.
4. Gesture dispatch may be evaluated later only as an isolated fallback if it becomes necessary.

Fixed coordinates are never allowed.

## Exclusions From the First Version

Fixed coordinates are excluded because YouTube layouts vary by device, density, orientation, locale, and app version.

OCR is excluded because the intended implementation should rely on accessibility semantics, not visual scraping.

Screenshots and screen recording are excluded to keep privacy boundaries tight and avoid unnecessary sensitive-data handling.
