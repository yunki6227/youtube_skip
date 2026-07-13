package com.yunki.youtubeskip.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NodeClickTargetPolicyTest {
    @Test
    fun choosesCandidateWhenCandidateSupportsClickAction() {
        val choice = NodeClickTargetPolicy.choose(
            listOf(
                candidate(distance = 0, supportsActionClick = true),
                candidate(distance = 1, supportsActionClick = true),
            ),
        )

        assertEquals(0, choice?.ancestorDistance)
        assertEquals(NodeClickTargetType.ACTION_CLICK, choice?.targetType)
    }

    @Test
    fun choosesNearestAncestorWithClickAction() {
        val choice = NodeClickTargetPolicy.choose(
            listOf(
                candidate(distance = 0),
                candidate(distance = 1),
                candidate(distance = 2, supportsActionClick = true),
                candidate(distance = 3, supportsActionClick = true),
            ),
        )

        assertEquals(2, choice?.ancestorDistance)
        assertEquals(NodeClickTargetType.ACTION_CLICK, choice?.targetType)
    }

    @Test
    fun choosesClickableOnlyFallbackWhenNoClickActionExists() {
        val choice = NodeClickTargetPolicy.choose(
            listOf(
                candidate(distance = 0),
                candidate(distance = 1, isClickable = true),
                candidate(distance = 2, isClickable = true),
            ),
        )

        assertEquals(1, choice?.ancestorDistance)
        assertEquals(NodeClickTargetType.CLICKABLE_FLAG_ONLY, choice?.targetType)
    }

    @Test
    fun rejectsDisabledAndInvisibleAncestors() {
        val choice = NodeClickTargetPolicy.choose(
            listOf(
                candidate(distance = 0),
                candidate(distance = 1, isEnabled = false, supportsActionClick = true),
                candidate(distance = 2, isVisibleToUser = false, supportsActionClick = true),
                candidate(distance = 3, supportsActionClick = true),
            ),
        )

        assertEquals(3, choice?.ancestorDistance)
    }

    @Test
    fun enforcesAncestorLimit() {
        val choice = NodeClickTargetPolicy.choose(
            listOf(
                candidate(distance = 0),
                candidate(distance = 5, supportsActionClick = true),
            ),
            maxAncestorDistance = 4,
        )

        assertNull(choice)
    }

    private fun candidate(
        distance: Int,
        isEnabled: Boolean = true,
        isVisibleToUser: Boolean = true,
        isClickable: Boolean = false,
        supportsActionClick: Boolean = false,
    ): NodeClickTargetCandidate {
        return NodeClickTargetCandidate(
            ancestorDistance = distance,
            isEnabled = isEnabled,
            isVisibleToUser = isVisibleToUser,
            isClickable = isClickable,
            supportsActionClick = supportsActionClick,
        )
    }
}
