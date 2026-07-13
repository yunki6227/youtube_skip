package com.yunki.youtubeskip.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.yunki.youtubeskip.detection.SkipButtonDetector
import com.yunki.youtubeskip.detection.SkipButtonDetectionOutcome
import com.yunki.youtubeskip.settings.AppPreferences
import com.yunki.youtubeskip.settings.LastClickResult
import com.yunki.youtubeskip.util.AppLogger

class YouTubeAccessibilityService : AccessibilityService() {
    private val eventLogThrottle = AccessibilityEventLogThrottle()
    private val eventProcessingDebounce = AccessibilityEventProcessingDebounce()
    private val successfulClickCooldown = SuccessfulClickCooldown()
    private val nodeScanThrottle = AccessibilityNodeScanThrottle()
    private val skipCandidateDiagnosticDeduplicator = SkipCandidateDiagnosticDeduplicator()
    private val nodeScanner = AccessibilityNodeScanner()
    private val skipButtonDetector = SkipButtonDetector()
    private val nodeClickExecutor = NodeClickExecutor()
    private val verificationHandler = Handler(Looper.getMainLooper())
    private val appPreferences: AppPreferences by lazy { AppPreferences(this) }
    private var pendingVerificationRunnable: Runnable? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName != YOUTUBE_PACKAGE_NAME) {
            return
        }

        val eventTypeName = AccessibilityEventLogPolicy.eventTypeName(event.eventType) ?: return
        val eventTimeMillis = event.eventTime.takeIf { it > 0L } ?: SystemClock.uptimeMillis()
        if (AppLogger.isDetailedAccessibilityDiagnosticsEnabled &&
            eventLogThrottle.shouldLog(event.eventType, eventTimeMillis)
        ) {
            AppLogger.debug(
                "accessibilityEvent type=$eventTypeName package=$packageName eventTime=$eventTimeMillis",
            )
        }

        processSkipClick(eventTypeName)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        pendingVerificationRunnable?.let(verificationHandler::removeCallbacks)
        pendingVerificationRunnable = null
        super.onDestroy()
    }

    private fun processSkipClick(eventTypeName: String) {
        if (!appPreferences.isAutomaticSkipEnabled()) {
            return
        }

        val nowMillis = SystemClock.uptimeMillis()
        if (successfulClickCooldown.isActive(nowMillis)) {
            if (successfulClickCooldown.shouldLogSuppression(nowMillis)) {
                AppLogger.logSkipProcessingSuppressed("successful_click_cooldown")
            }
            return
        }

        if (!eventProcessingDebounce.shouldProcess(nowMillis)) {
            return
        }

        val root = rootInActiveWindow
        if (root == null) {
            AppLogger.logSkipDetectionResult("root_unavailable")
            appPreferences.recordClickResult(LastClickResult.TARGET_UNAVAILABLE)
            logNodeScanRootNull(eventTypeName)
            return
        }

        val detectionOutcome = skipButtonDetector.detectOutcome(root)
        maybeLogNodeScan(root, eventTypeName, nowMillis)

        val detection = when (detectionOutcome) {
            is SkipButtonDetectionOutcome.Detected -> detectionOutcome.result
            SkipButtonDetectionOutcome.NoSemanticCandidate -> return
            SkipButtonDetectionOutcome.NoValidClickTarget -> {
                AppLogger.logSkipDetectionResult("no_valid_click_target")
                appPreferences.recordClickResult(LastClickResult.NO_VALID_CLICK_TARGET)
                return
            }
            is SkipButtonDetectionOutcome.Exception -> {
                AppLogger.logSkipDetectionResult(
                    result = "exception",
                    exceptionClassName = detectionOutcome.exceptionClassName,
                )
                appPreferences.recordClickResult(LastClickResult.EXCEPTION)
                return
            }
        }

        AppLogger.logSkipDetected(detection)
        AppLogger.logSkipClickAttempt()
        val clickResult = nodeClickExecutor.click(detection.targetNode)
        AppLogger.logSkipClickResult(clickResult)
        appPreferences.recordClickResult(clickResult.toLastClickResult())

        if (clickResult.isSuccess) {
            successfulClickCooldown.recordSuccessfulClick(nowMillis)
            schedulePostClickVerification()
        }
    }

    private fun maybeLogNodeScan(
        root: android.view.accessibility.AccessibilityNodeInfo,
        eventTypeName: String,
        nowMillis: Long,
    ) {
        if (!AppLogger.isDetailedAccessibilityDiagnosticsEnabled) {
            return
        }

        if (!nodeScanThrottle.shouldScan(nowMillis)) {
            return
        }

        val result = nodeScanner.scan(root)
        result.skipCandidateDiagnostics
            .filter { diagnostic ->
                skipCandidateDiagnosticDeduplicator.shouldLog(
                    signature = diagnostic.signature(),
                    nowMillis = nowMillis,
                )
            }
            .forEach(AppLogger::logSkipCandidateDiagnostic)
        AppLogger.logNodeScanSummary(result, eventTypeName)
    }

    private fun logNodeScanRootNull(eventTypeName: String) {
        if (AppLogger.isDetailedAccessibilityDiagnosticsEnabled) {
            AppLogger.logNodeScanRootNull(eventTypeName)
        }
    }

    private fun schedulePostClickVerification() {
        pendingVerificationRunnable?.let(verificationHandler::removeCallbacks)

        val runnable = Runnable {
            pendingVerificationRunnable = null
            verifySkipCandidateState()
        }

        pendingVerificationRunnable = runnable
        verificationHandler.postDelayed(runnable, VERIFICATION_DELAY_MILLIS)
    }

    private fun verifySkipCandidateState() {
        val root = rootInActiveWindow
        val result = if (root == null) {
            SkipVerificationPolicy.resultFor(
                rootAvailable = false,
                candidateStillPresent = false,
            )
        } else {
            SkipVerificationPolicy.resultFor(
                rootAvailable = true,
                candidateStillPresent = skipButtonDetector.containsSemanticSkipCandidate(root),
            )
        }

        AppLogger.logSkipVerification(result)
    }

    private companion object {
        const val YOUTUBE_PACKAGE_NAME = "com.google.android.youtube"
        const val VERIFICATION_DELAY_MILLIS = 600L
    }
}

private fun NodeClickExecutionResult.toLastClickResult(): LastClickResult {
    return when (status) {
        NodeClickExecutionStatus.SUCCESS -> LastClickResult.SUCCESS
        NodeClickExecutionStatus.ACTION_RETURNED_FALSE -> LastClickResult.ACTION_RETURNED_FALSE
        NodeClickExecutionStatus.TARGET_ACTION_UNAVAILABLE -> LastClickResult.NO_VALID_CLICK_TARGET
        NodeClickExecutionStatus.TARGET_STALE_OR_UNAVAILABLE,
        NodeClickExecutionStatus.TARGET_DISABLED,
        NodeClickExecutionStatus.TARGET_NOT_VISIBLE,
        -> LastClickResult.TARGET_UNAVAILABLE
        NodeClickExecutionStatus.EXCEPTION -> LastClickResult.EXCEPTION
    }
}
