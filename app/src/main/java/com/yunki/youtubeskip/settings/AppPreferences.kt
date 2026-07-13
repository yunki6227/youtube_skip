package com.yunki.youtubeskip.settings

import android.content.Context

class AppPreferences(context: Context) {
    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun isAutomaticSkipEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTOMATIC_SKIP_ENABLED, true)
    }

    fun setAutomaticSkipEnabled(enabled: Boolean) {
        sharedPreferences
            .edit()
            .putBoolean(KEY_AUTOMATIC_SKIP_ENABLED, enabled)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "youtube_skip_preferences"
        const val KEY_AUTOMATIC_SKIP_ENABLED = "automatic_skip_enabled"
    }
}
