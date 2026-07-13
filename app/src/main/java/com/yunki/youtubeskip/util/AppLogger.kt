package com.yunki.youtubeskip.util

import android.util.Log
import com.yunki.youtubeskip.BuildConfig
import com.yunki.youtubeskip.accessibility.AccessibilityNodeScanResult
import com.yunki.youtubeskip.accessibility.AccessibilityNodeSnapshot
import com.yunki.youtubeskip.accessibility.NodeClickExecutionResult
import com.yunki.youtubeskip.accessibility.SkipVerificationResult
import com.yunki.youtubeskip.accessibility.SkipCandidateDiagnostic
import com.yunki.youtubeskip.detection.SkipButtonDetectionResult

object AppLogger {
    private const val TAG = "YouTubeSkip"
    const val DETAILED_ACCESSIBILITY_DIAGNOSTICS = false

    val isDebugBuild: Boolean
        get() = BuildConfig.DEBUG

    val isDetailedAccessibilityDiagnosticsEnabled: Boolean
        get() = isDebugBuild && DETAILED_ACCESSIBILITY_DIAGNOSTICS

    fun debug(message: String) {
        if (isDebugBuild) {
            Log.d(TAG, message)
        }
    }

    fun logNodeSnapshot(snapshot: AccessibilityNodeSnapshot) {
        debug(snapshot.toLogLine())
    }

    fun logNodeScanSummary(result: AccessibilityNodeScanResult, eventTypeName: String) {
        debug(result.toSummaryLogLine(eventTypeName))
    }

    fun logNodeScanRootNull(eventTypeName: String) {
        debug("nodeScan root=null event=$eventTypeName")
    }

    fun logSkipCandidateDiagnostic(diagnostic: SkipCandidateDiagnostic) {
        debug(diagnostic.candidateLogLine())
        diagnostic.ancestors.forEach { ancestor ->
            debug(ancestor.toLogLine())
        }
        debug(diagnostic.targetLogLine())
    }

    fun logSkipDetected(result: SkipButtonDetectionResult) {
        debug(
            "skipDetected " +
                "label=\"${result.matchedLabel}\" " +
                "source=${result.labelSource.logName} " +
                "candidateClass=${result.candidateClassName ?: "null"} " +
                "ancestorDistance=${result.ancestorDistance} " +
                "targetClass=${result.clickTarget.className ?: "null"} " +
                "targetBounds=${result.targetBoundsInScreen}",
        )
    }

    fun logSkipClickAttempt() {
        debug("skipClick attempted action=ACTION_CLICK")
    }

    fun logSkipClickResult(result: NodeClickExecutionResult) {
        val exceptionSuffix = result.exceptionClassName?.let { " exception=$it" }.orEmpty()
        debug("skipClick result=${result.status.logName}$exceptionSuffix")
    }

    fun logSkipDetectionResult(
        result: String,
        exceptionClassName: String? = null,
    ) {
        val exceptionSuffix = exceptionClassName?.let { " exception=$it" }.orEmpty()
        debug("skipDetection result=$result$exceptionSuffix")
    }

    fun logSkipProcessingSuppressed(reason: String) {
        debug("skipProcessing suppressed=$reason")
    }

    fun logSkipVerification(result: SkipVerificationResult) {
        debug("skipVerification result=${result.logName}")
    }
}
