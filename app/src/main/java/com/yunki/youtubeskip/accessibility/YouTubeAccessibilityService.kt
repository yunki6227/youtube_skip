package com.yunki.youtubeskip.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.yunki.youtubeskip.util.AppLogger

class YouTubeAccessibilityService : AccessibilityService() {
    private val eventLogThrottle = AccessibilityEventLogThrottle()
    private val nodeScanThrottle = AccessibilityNodeScanThrottle()
    private val nodeScanner = AccessibilityNodeScanner()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName != YOUTUBE_PACKAGE_NAME) {
            return
        }

        val eventTypeName = AccessibilityEventLogPolicy.eventTypeName(event.eventType) ?: return
        val eventTimeMillis = event.eventTime.takeIf { it > 0L } ?: SystemClock.uptimeMillis()
        if (eventLogThrottle.shouldLog(event.eventType, eventTimeMillis)) {
            AppLogger.debug(
                "accessibilityEvent type=$eventTypeName package=$packageName eventTime=$eventTimeMillis",
            )
        }

        maybeLogNodeScan(eventTypeName)
    }

    override fun onInterrupt() = Unit

    private fun maybeLogNodeScan(eventTypeName: String) {
        if (!AppLogger.isDebugBuild) {
            return
        }

        val nowMillis = SystemClock.uptimeMillis()
        if (!nodeScanThrottle.shouldScan(nowMillis)) {
            return
        }

        val root = rootInActiveWindow
        if (root == null) {
            AppLogger.logNodeScanRootNull(eventTypeName)
            return
        }

        val result = nodeScanner.scan(root)
        result.snapshots.forEach(AppLogger::logNodeSnapshot)
        AppLogger.logNodeScanSummary(result, eventTypeName)
    }

    private companion object {
        const val YOUTUBE_PACKAGE_NAME = "com.google.android.youtube"
    }
}
