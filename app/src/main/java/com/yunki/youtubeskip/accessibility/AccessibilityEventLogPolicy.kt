package com.yunki.youtubeskip.accessibility

import android.view.accessibility.AccessibilityEvent

object AccessibilityEventLogPolicy {
    fun eventTypeName(eventType: Int): String? {
        return when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "WINDOW_CONTENT_CHANGED"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "WINDOW_STATE_CHANGED"
            else -> null
        }
    }
}

class AccessibilityEventLogThrottle(
    private val throttleWindowMillis: Long = DEFAULT_THROTTLE_WINDOW_MILLIS,
) {
    private val lastLogTimeByEventType = mutableMapOf<Int, Long>()

    fun shouldLog(eventType: Int, eventTimeMillis: Long): Boolean {
        AccessibilityEventLogPolicy.eventTypeName(eventType) ?: return false

        val lastLogTime = lastLogTimeByEventType[eventType]
        if (lastLogTime != null && eventTimeMillis - lastLogTime < throttleWindowMillis) {
            return false
        }

        lastLogTimeByEventType[eventType] = eventTimeMillis
        return true
    }

    private companion object {
        const val DEFAULT_THROTTLE_WINDOW_MILLIS = 300L
    }
}
