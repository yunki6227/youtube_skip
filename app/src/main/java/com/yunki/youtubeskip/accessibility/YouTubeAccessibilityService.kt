package com.yunki.youtubeskip.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class YouTubeAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.toString() != YOUTUBE_PACKAGE_NAME) {
            return
        }

        // Stage 2 only: service registration without detection, traversal, clicking, or content logging.
    }

    override fun onInterrupt() = Unit

    private companion object {
        const val YOUTUBE_PACKAGE_NAME = "com.google.android.youtube"
    }
}
