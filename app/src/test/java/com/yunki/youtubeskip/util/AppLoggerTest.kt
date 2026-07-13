package com.yunki.youtubeskip.util

import org.junit.Assert.assertFalse
import org.junit.Test

class AppLoggerTest {
    @Test
    fun detailedAccessibilityDiagnosticsAreDisabledByDefault() {
        assertFalse(AppLogger.DETAILED_ACCESSIBILITY_DIAGNOSTICS)
        assertFalse(AppLogger.isDetailedAccessibilityDiagnosticsEnabled)
    }
}
