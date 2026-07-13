package com.yunki.youtubeskip.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityNodeSnapshotTest {
    @Test
    fun normalizeLogTextTrimsAndCollapsesWhitespace() {
        assertEquals(
            "Skip ad now",
            AccessibilityNodeSnapshot.normalizeLogText("  Skip\tad\n\nnow  "),
        )
    }

    @Test
    fun normalizeLogTextReturnsNullForBlankText() {
        assertNull(AccessibilityNodeSnapshot.normalizeLogText(" \n\t "))
    }

    @Test
    fun normalizeLogTextTruncatesToEightyCharacters() {
        val normalized = AccessibilityNodeSnapshot.normalizeLogText("x".repeat(120))

        assertEquals(AccessibilityNodeSnapshot.MAX_LOG_FIELD_LENGTH, normalized?.length)
        assertEquals("x".repeat(80), normalized)
    }

    @Test
    fun blankNonClickableSnapshotIsNotUseful() {
        val snapshot = AccessibilityNodeSnapshot(
            depth = 0,
            text = null,
            contentDescription = null,
            className = "android.view.View",
            viewIdResourceName = null,
            isClickable = false,
            isEnabled = true,
            boundsInScreen = "[0,0][0,0]",
        )

        assertFalse(snapshot.hasUsefulField())
    }

    @Test
    fun clickableSnapshotIsUsefulWithoutText() {
        val snapshot = AccessibilityNodeSnapshot(
            depth = 0,
            text = null,
            contentDescription = null,
            className = "android.widget.Button",
            viewIdResourceName = null,
            isClickable = true,
            isEnabled = true,
            boundsInScreen = "[0,0][10,10]",
        )

        assertTrue(snapshot.hasUsefulField())
    }

    @Test
    fun summaryIncludesSnapshotCountAndTruncatedFlag() {
        val result = AccessibilityNodeScanResult(
            snapshots = listOf(
                AccessibilityNodeSnapshot(
                    depth = 1,
                    text = "Skip",
                    contentDescription = null,
                    className = "android.widget.Button",
                    viewIdResourceName = null,
                    isClickable = true,
                    isEnabled = true,
                    boundsInScreen = "[1,2][3,4]",
                ),
            ),
            visitedNodeCount = 200,
            truncated = true,
        )

        assertEquals(
            "nodeScan count=1 visited=200 truncated=true event=WINDOW_CONTENT_CHANGED",
            result.toSummaryLogLine("WINDOW_CONTENT_CHANGED"),
        )
    }
}
