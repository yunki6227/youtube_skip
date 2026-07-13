package com.yunki.youtubeskip.accessibility

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityNodeScanThrottleTest {
    @Test
    fun throttleAllowsFirstScan() {
        val throttle = AccessibilityNodeScanThrottle(throttleWindowMillis = 1_000L)

        assertTrue(throttle.shouldScan(nowMillis = 5_000L))
    }

    @Test
    fun throttleSuppressesScansInsideWindow() {
        val throttle = AccessibilityNodeScanThrottle(throttleWindowMillis = 1_000L)

        assertTrue(throttle.shouldScan(nowMillis = 5_000L))
        assertFalse(throttle.shouldScan(nowMillis = 5_999L))
    }

    @Test
    fun throttleAllowsScanAtWindowBoundary() {
        val throttle = AccessibilityNodeScanThrottle(throttleWindowMillis = 1_000L)

        assertTrue(throttle.shouldScan(nowMillis = 5_000L))
        assertTrue(throttle.shouldScan(nowMillis = 6_000L))
    }
}
