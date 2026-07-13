package com.yunki.youtubeskip.detection

import org.junit.Assert.assertEquals
import org.junit.Test

class SkipButtonCandidateDeduplicatorTest {
    @Test
    fun keepsOneCandidatePerTargetSignatureUsingPriorityThenTraversalOrder() {
        val candidates = listOf(
            SkipButtonCandidateRanking(
                priority = 4,
                traversalOrder = 0,
                targetSignature = "target-a",
            ),
            SkipButtonCandidateRanking(
                priority = 1,
                traversalOrder = 1,
                targetSignature = "target-a",
            ),
            SkipButtonCandidateRanking(
                priority = 2,
                traversalOrder = 2,
                targetSignature = "target-b",
            ),
        )

        val uniqueCandidates = SkipButtonCandidateDeduplicator.selectPreferredUnique(candidates)

        assertEquals(2, uniqueCandidates.size)
        assertEquals(1, uniqueCandidates[0].priority)
        assertEquals("target-a", uniqueCandidates[0].targetSignature)
        assertEquals("target-b", uniqueCandidates[1].targetSignature)
    }

    @Test
    fun traversalOrderBreaksPriorityTies() {
        val uniqueCandidates = SkipButtonCandidateDeduplicator.selectPreferredUnique(
            listOf(
                SkipButtonCandidateRanking(
                    priority = 1,
                    traversalOrder = 2,
                    targetSignature = "target-b",
                ),
                SkipButtonCandidateRanking(
                    priority = 1,
                    traversalOrder = 1,
                    targetSignature = "target-a",
                ),
            ),
        )

        assertEquals("target-a", uniqueCandidates.first().targetSignature)
    }
}
