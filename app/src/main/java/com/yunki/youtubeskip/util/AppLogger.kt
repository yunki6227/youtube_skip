package com.yunki.youtubeskip.util

import android.util.Log
import com.yunki.youtubeskip.BuildConfig

object AppLogger {
    private const val TAG = "YouTubeSkip"

    fun debug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }
}
