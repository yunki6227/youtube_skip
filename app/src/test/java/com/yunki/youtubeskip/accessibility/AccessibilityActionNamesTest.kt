package com.yunki.youtubeskip.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityActionNamesTest {
    @Test
    fun mapsCoreActionIdsToReadableNames() {
        assertEquals("ACTION_CLICK", AccessibilityActionNames.nameFor(AccessibilityNodeInfo.ACTION_CLICK))
        assertEquals("ACTION_DISMISS", AccessibilityActionNames.nameFor(AccessibilityNodeInfo.ACTION_DISMISS))
        assertEquals("ACTION_FOCUS", AccessibilityActionNames.nameFor(AccessibilityNodeInfo.ACTION_FOCUS))
    }

    @Test
    fun mapsUnknownActionIdsToStableFallbackName() {
        assertEquals("ACTION_123456", AccessibilityActionNames.nameFor(123_456))
    }

    @Test
    fun extractsKnownIdsFromLegacyBitmask() {
        val actionIds = AccessibilityActionNames.actionIdsFromLegacyBitmask(
            AccessibilityNodeInfo.ACTION_CLICK or AccessibilityNodeInfo.ACTION_FOCUS,
        )

        assertTrue(actionIds.contains(AccessibilityNodeInfo.ACTION_CLICK))
        assertTrue(actionIds.contains(AccessibilityNodeInfo.ACTION_FOCUS))
    }
}
