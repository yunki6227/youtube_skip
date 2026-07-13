package com.yunki.youtubeskip.accessibility

class AccessibilityNodeScanThrottle(
    private val throttleWindowMillis: Long = DEFAULT_THROTTLE_WINDOW_MILLIS,
) {
    private var lastScanTimeMillis: Long? = null

    fun shouldScan(nowMillis: Long): Boolean {
        val lastScanTime = lastScanTimeMillis
        if (lastScanTime != null && nowMillis - lastScanTime < throttleWindowMillis) {
            return false
        }

        lastScanTimeMillis = nowMillis
        return true
    }

    private companion object {
        const val DEFAULT_THROTTLE_WINDOW_MILLIS = 1_000L
    }
}
