package com.yunki.youtubeskip.settings

import android.content.Context

enum class LastClickResult(
    val displayText: String,
) {
    NONE("None yet"),
    SUCCESS("Success"),
    ACTION_RETURNED_FALSE("Action returned false"),
    NO_VALID_CLICK_TARGET("No valid click target"),
    TARGET_UNAVAILABLE("Target unavailable"),
    EXCEPTION("Exception");

    companion object {
        fun fromStoredValue(value: String?): LastClickResult {
            return entries.firstOrNull { it.name == value } ?: NONE
        }
    }
}

data class SkipStatistics(
    val successfulSkipCount: Int,
    val lastSuccessfulSkipTimestampMillis: Long?,
    val lastClickResult: LastClickResult,
)

class AppPreferences(context: Context) {
    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    val automaticSkipEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTOMATIC_SKIP_ENABLED, true)

    fun isAutomaticSkipEnabled(): Boolean {
        return automaticSkipEnabled
    }

    fun setAutomaticSkipEnabled(enabled: Boolean) {
        sharedPreferences
            .edit()
            .putBoolean(KEY_AUTOMATIC_SKIP_ENABLED, enabled)
            .commit()
    }

    fun skipStatistics(): SkipStatistics {
        val timestampMillis = sharedPreferences.getLong(
            KEY_LAST_SUCCESSFUL_SKIP_TIMESTAMP_MILLIS,
            NO_TIMESTAMP,
        ).takeIf { it != NO_TIMESTAMP }

        return SkipStatistics(
            successfulSkipCount = sharedPreferences.getInt(KEY_SUCCESSFUL_SKIP_COUNT, 0),
            lastSuccessfulSkipTimestampMillis = timestampMillis,
            lastClickResult = LastClickResult.fromStoredValue(
                sharedPreferences.getString(KEY_LAST_CLICK_RESULT, null),
            ),
        )
    }

    @Synchronized
    fun recordClickResult(
        result: LastClickResult,
        timestampMillis: Long = System.currentTimeMillis(),
    ) {
        val currentStatistics = skipStatistics()
        val successfulSkipCount = if (result == LastClickResult.SUCCESS) {
            currentStatistics.successfulSkipCount + 1
        } else {
            currentStatistics.successfulSkipCount
        }
        val lastSuccessfulSkipTimestampMillis = if (result == LastClickResult.SUCCESS) {
            timestampMillis
        } else {
            currentStatistics.lastSuccessfulSkipTimestampMillis
        }

        sharedPreferences
            .edit()
            .putInt(KEY_SUCCESSFUL_SKIP_COUNT, successfulSkipCount)
            .putLong(
                KEY_LAST_SUCCESSFUL_SKIP_TIMESTAMP_MILLIS,
                lastSuccessfulSkipTimestampMillis ?: NO_TIMESTAMP,
            )
            .putString(KEY_LAST_CLICK_RESULT, result.name)
            .commit()
    }

    fun resetStatistics() {
        sharedPreferences
            .edit()
            .putInt(KEY_SUCCESSFUL_SKIP_COUNT, 0)
            .putLong(KEY_LAST_SUCCESSFUL_SKIP_TIMESTAMP_MILLIS, NO_TIMESTAMP)
            .putString(KEY_LAST_CLICK_RESULT, LastClickResult.NONE.name)
            .commit()
    }

    internal fun clearAllForTesting() {
        sharedPreferences
            .edit()
            .clear()
            .commit()
    }

    private companion object {
        const val PREFERENCES_NAME = "youtube_skip_preferences"
        const val KEY_AUTOMATIC_SKIP_ENABLED = "automatic_skip_enabled"
        const val KEY_SUCCESSFUL_SKIP_COUNT = "successful_skip_count"
        const val KEY_LAST_SUCCESSFUL_SKIP_TIMESTAMP_MILLIS = "last_successful_skip_timestamp_millis"
        const val KEY_LAST_CLICK_RESULT = "last_click_result"
        const val NO_TIMESTAMP = -1L
    }
}
