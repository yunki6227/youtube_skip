package com.yunki.youtubeskip.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import com.yunki.youtubeskip.detection.SkipCandidateMatch
import com.yunki.youtubeskip.detection.SkipLabelSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SkipCandidateDiagnosticTest {
    @Test
    fun classifiesClickTargets() {
        assertEquals(
            ClickTargetClassification.DISABLED,
            SkipAncestorDiagnostic.classifyClickTarget(
                isEnabled = false,
                isVisibleToUser = true,
                isClickable = true,
                supportsClick = true,
            ),
        )
        assertEquals(
            ClickTargetClassification.NOT_VISIBLE,
            SkipAncestorDiagnostic.classifyClickTarget(
                isEnabled = true,
                isVisibleToUser = false,
                isClickable = true,
                supportsClick = true,
            ),
        )
        assertEquals(
            ClickTargetClassification.DIRECT_CLICK_ACTION,
            SkipAncestorDiagnostic.classifyClickTarget(
                isEnabled = true,
                isVisibleToUser = true,
                isClickable = false,
                supportsClick = true,
            ),
        )
        assertEquals(
            ClickTargetClassification.CLICKABLE_FLAG_ONLY,
            SkipAncestorDiagnostic.classifyClickTarget(
                isEnabled = true,
                isVisibleToUser = true,
                isClickable = true,
                supportsClick = false,
            ),
        )
        assertEquals(
            ClickTargetClassification.NON_CLICKABLE,
            SkipAncestorDiagnostic.classifyClickTarget(
                isEnabled = true,
                isVisibleToUser = true,
                isClickable = false,
                supportsClick = false,
            ),
        )
    }

    @Test
    fun recommendsCandidateWhenCandidateSupportsClickAction() {
        val diagnostic = diagnosticFor(
            ancestor(level = "candidate", actionIds = listOf(AccessibilityNodeInfo.ACTION_CLICK)),
            ancestor(level = "parent-1", isClickable = true),
        )

        assertEquals("candidate", diagnostic.recommendedDiagnosticTargetLevel)
        assertEquals("candidate", diagnostic.firstClickActionAncestorLevel)
        assertEquals(
            SkipTargetRecommendationReason.CANDIDATE_ENABLED_VISIBLE_CLICK_ACTION,
            diagnostic.recommendationReason,
        )
    }

    @Test
    fun recommendsNearestAncestorWithClickActionBeforeClickableFlagOnlyAncestor() {
        val diagnostic = diagnosticFor(
            ancestor(level = "candidate"),
            ancestor(level = "parent-1", isClickable = true),
            ancestor(
                level = "parent-2",
                isClickable = true,
                actionIds = listOf(AccessibilityNodeInfo.ACTION_CLICK),
            ),
        )

        assertEquals("parent-2", diagnostic.recommendedDiagnosticTargetLevel)
        assertEquals("parent-1", diagnostic.firstClickableAncestorLevel)
        assertEquals("parent-2", diagnostic.firstClickActionAncestorLevel)
        assertEquals(
            SkipTargetRecommendationReason.NEAREST_ENABLED_VISIBLE_CLICK_ACTION,
            diagnostic.recommendationReason,
        )
    }

    @Test
    fun recommendsNearestClickableAncestorWhenNoClickActionExists() {
        val diagnostic = diagnosticFor(
            ancestor(level = "candidate"),
            ancestor(level = "parent-1", isClickable = true),
        )

        assertEquals("parent-1", diagnostic.recommendedDiagnosticTargetLevel)
        assertEquals("parent-1", diagnostic.firstClickableAncestorLevel)
        assertNull(diagnostic.firstClickActionAncestorLevel)
        assertEquals(
            SkipTargetRecommendationReason.NEAREST_ENABLED_VISIBLE_CLICKABLE,
            diagnostic.recommendationReason,
        )
    }

    @Test
    fun reportsNoRecommendationWhenNoEnabledVisibleClickTargetExists() {
        val diagnostic = diagnosticFor(
            ancestor(level = "candidate", isVisibleToUser = false),
            ancestor(level = "parent-1", isEnabled = false, isClickable = true),
        )

        assertNull(diagnostic.recommendedDiagnosticTargetLevel)
        assertEquals(SkipTargetRecommendationReason.NONE, diagnostic.recommendationReason)
    }

    @Test
    fun signatureIncludesRecommendedTarget() {
        val candidate = ancestor(level = "candidate", boundsInScreen = "[1,2][3,4]")
        val clickableParent = ancestor(level = "parent-1", isClickable = true)
        val nonClickableParent = ancestor(level = "parent-1")
        val clickActionGrandparent = ancestor(
            level = "parent-2",
            isClickable = true,
            actionIds = listOf(AccessibilityNodeInfo.ACTION_CLICK),
        )

        assertNotEquals(
            diagnosticFor(candidate, clickableParent).signature(),
            diagnosticFor(candidate, nonClickableParent, clickActionGrandparent).signature(),
        )
    }

    @Test
    fun deduplicatorSuppressesIdenticalSignaturesInsideWindow() {
        val deduplicator = SkipCandidateDiagnosticDeduplicator(suppressionWindowMillis = 2_000L)

        assertTrue(deduplicator.shouldLog(signature = "skip|[1,2][3,4]", nowMillis = 10_000L))
        assertFalse(deduplicator.shouldLog(signature = "skip|[1,2][3,4]", nowMillis = 11_999L))
        assertTrue(deduplicator.shouldLog(signature = "skip|[1,2][3,4]", nowMillis = 12_000L))
    }

    @Test
    fun deduplicatorAllowsDifferentSignaturesInsideWindow() {
        val deduplicator = SkipCandidateDiagnosticDeduplicator(suppressionWindowMillis = 2_000L)

        assertTrue(deduplicator.shouldLog(signature = "skip|one", nowMillis = 10_000L))
        assertTrue(deduplicator.shouldLog(signature = "skip|two", nowMillis = 10_100L))
    }

    private fun diagnosticFor(vararg ancestors: SkipAncestorDiagnostic): SkipCandidateDiagnostic {
        return SkipCandidateDiagnostic.create(
            match = SkipCandidateMatch(
                labelSource = SkipLabelSource.CONTENT_DESCRIPTION,
                normalizedLabel = "skip ad",
            ),
            depth = 7,
            ancestors = ancestors.toList(),
        )
    }

    private fun ancestor(
        level: String,
        isClickable: Boolean = false,
        isEnabled: Boolean = true,
        isVisibleToUser: Boolean = true,
        boundsInScreen: String = "[0,0][1,1]",
        actionIds: List<Int> = emptyList(),
    ): SkipAncestorDiagnostic {
        return SkipAncestorDiagnostic(
            level = level,
            text = null,
            contentDescription = null,
            className = "android.widget.FrameLayout",
            viewIdResourceName = null,
            isClickable = isClickable,
            isEnabled = isEnabled,
            isFocusable = false,
            isVisibleToUser = isVisibleToUser,
            boundsInScreen = boundsInScreen,
            actionIds = actionIds,
            actionNames = AccessibilityActionNames.namesFor(actionIds),
        )
    }
}
