package com.yunki.youtubeskip.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class AppPreferencesTest {
    private lateinit var preferences: AppPreferences

    @Before
    fun setUp() {
        preferences = AppPreferences(RuntimeEnvironment.getApplication())
        preferences.clearAllForTesting()
    }

    @Test
    fun automaticSkipDefaultsToEnabled() {
        assertTrue(preferences.automaticSkipEnabled)
    }

    @Test
    fun changingAutomaticSkipPersists() {
        preferences.setAutomaticSkipEnabled(false)

        val newPreferences = AppPreferences(RuntimeEnvironment.getApplication())

        assertEquals(false, newPreferences.automaticSkipEnabled)
    }

    @Test
    fun disabledAutomaticSkipSettingCanGateProductionFlow() {
        preferences.setAutomaticSkipEnabled(false)

        assertEquals(false, preferences.isAutomaticSkipEnabled())
    }

    @Test
    fun successCountIncrementsOnlyOnSuccessfulClick() {
        preferences.recordClickResult(LastClickResult.ACTION_RETURNED_FALSE, timestampMillis = 1_000L)
        preferences.recordClickResult(LastClickResult.SUCCESS, timestampMillis = 2_000L)
        preferences.recordClickResult(LastClickResult.TARGET_UNAVAILABLE, timestampMillis = 3_000L)

        val statistics = preferences.skipStatistics()

        assertEquals(1, statistics.successfulSkipCount)
        assertEquals(2_000L, statistics.lastSuccessfulSkipTimestampMillis)
        assertEquals(LastClickResult.TARGET_UNAVAILABLE, statistics.lastClickResult)
    }

    @Test
    fun failedClickDoesNotUpdateLastSuccessfulTimestamp() {
        preferences.recordClickResult(LastClickResult.SUCCESS, timestampMillis = 2_000L)
        preferences.recordClickResult(LastClickResult.NO_VALID_CLICK_TARGET, timestampMillis = 3_000L)

        val statistics = preferences.skipStatistics()

        assertEquals(1, statistics.successfulSkipCount)
        assertEquals(2_000L, statistics.lastSuccessfulSkipTimestampMillis)
        assertEquals(LastClickResult.NO_VALID_CLICK_TARGET, statistics.lastClickResult)
    }

    @Test
    fun emptyStatisticsUseNoneResultAndNoTimestamp() {
        val statistics = preferences.skipStatistics()

        assertEquals(0, statistics.successfulSkipCount)
        assertNull(statistics.lastSuccessfulSkipTimestampMillis)
        assertEquals(LastClickResult.NONE, statistics.lastClickResult)
    }

    @Test
    fun lastClickResultDisplayMappingUsesExpectedLabels() {
        assertEquals("Success", LastClickResult.SUCCESS.displayText)
        assertEquals("Action returned false", LastClickResult.ACTION_RETURNED_FALSE.displayText)
        assertEquals("No valid click target", LastClickResult.NO_VALID_CLICK_TARGET.displayText)
        assertEquals("Target unavailable", LastClickResult.TARGET_UNAVAILABLE.displayText)
        assertEquals("Exception", LastClickResult.EXCEPTION.displayText)
        assertEquals("None yet", LastClickResult.NONE.displayText)
    }

    @Test
    fun unknownStoredClickResultFallsBackToNone() {
        assertEquals(LastClickResult.NONE, LastClickResult.fromStoredValue("not-a-result"))
    }
}
