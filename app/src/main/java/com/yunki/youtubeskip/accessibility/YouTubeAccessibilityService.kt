package com.yunki.youtubeskip.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.yunki.youtubeskip.util.AppLogger

class YouTubeAccessibilityService : AccessibilityService() {
    private val eventLogThrottle = AccessibilityEventLogThrottle()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName != YOUTUBE_PACKAGE_NAME) {
            return
        }

        val eventTypeName = AccessibilityEventLogPolicy.eventTypeName(event.eventType) ?: return
        val eventTimeMillis = event.eventTime.takeIf { it > 0L } ?: SystemClock.uptimeMillis()
        if (!eventLogThrottle.shouldLog(event.eventType, eventTimeMillis)) {
            return
        }

        AppLogger.debug(
            "accessibilityEvent type=$eventTypeName package=$packageName eventTime=$eventTimeMillis",
        )
    }

    override fun onInterrupt() = Unit

    private companion object {
        const val YOUTUBE_PACKAGE_NAME = "com.google.android.youtube"
    }
}
