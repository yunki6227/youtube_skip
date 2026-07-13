package com.yunki.youtubeskip.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityProcessingGuardsTest {
    @Test
    fun eventDebounceSuppressesEventsInsideWindow() {
        val debounce = AccessibilityEventProcessingDebounce(debounceWindowMillis = 350L)

        assertTrue(debounce.shouldProcess(nowMillis = 1_000L))
        assertFalse(debounce.shouldProcess(nowMillis = 1_349L))
        assertTrue(debounce.shouldProcess(nowMillis = 1_350L))
    }

    @Test
    fun successfulClickCooldownStartsOnlyWhenRecorded() {
        val cooldown = SuccessfulClickCooldown(cooldownMillis = 3_000L)

        assertFalse(cooldown.isActive(nowMillis = 1_000L))

        cooldown.recordSuccessfulClick(nowMillis = 1_000L)

        assertTrue(cooldown.isActive(nowMillis = 3_999L))
        assertFalse(cooldown.isActive(nowMillis = 4_000L))
    }

    @Test
    fun cooldownSuppressionLogIsThrottled() {
        val cooldown = SuccessfulClickCooldown(
            cooldownMillis = 3_000L,
            suppressionLogWindowMillis = 1_000L,
        )
        cooldown.recordSuccessfulClick(nowMillis = 1_000L)

        assertTrue(cooldown.shouldLogSuppression(nowMillis = 1_100L))
        assertFalse(cooldown.shouldLogSuppression(nowMillis = 1_999L))
        assertTrue(cooldown.shouldLogSuppression(nowMillis = 2_100L))
    }

    @Test
    fun clickResultStatusesHaveStructuredLogNames() {
        assertEquals("success", NodeClickExecutionStatus.SUCCESS.logName)
        assertEquals("action_returned_false", NodeClickExecutionStatus.ACTION_RETURNED_FALSE.logName)
        assertEquals(
            "target_disabled",
            NodeClickExecutionStatus.TARGET_DISABLED.logName,
        )
        assertEquals(
            "target_not_visible",
            NodeClickExecutionStatus.TARGET_NOT_VISIBLE.logName,
        )
    }

    @Test
    fun verificationPolicyClassifiesRootAndCandidateState() {
        assertEquals(
            SkipVerificationResult.ROOT_UNAVAILABLE,
            SkipVerificationPolicy.resultFor(
                rootAvailable = false,
                candidateStillPresent = false,
            ),
        )
        assertEquals(
            SkipVerificationResult.CANDIDATE_STILL_PRESENT,
            SkipVerificationPolicy.resultFor(
                rootAvailable = true,
                candidateStillPresent = true,
            ),
        )
        assertEquals(
            SkipVerificationResult.CANDIDATE_DISAPPEARED,
            SkipVerificationPolicy.resultFor(
                rootAvailable = true,
                candidateStillPresent = false,
            ),
        )
    }
}
