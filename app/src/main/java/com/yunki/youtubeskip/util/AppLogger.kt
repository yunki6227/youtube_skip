package com.yunki.youtubeskip.util

import android.util.Log
import com.yunki.youtubeskip.BuildConfig
import com.yunki.youtubeskip.accessibility.AccessibilityNodeScanResult
import com.yunki.youtubeskip.accessibility.AccessibilityNodeSnapshot

object AppLogger {
    private const val TAG = "YouTubeSkip"

    val isDebugBuild: Boolean
        get() = BuildConfig.DEBUG

    fun debug(message: String) {
        if (isDebugBuild) {
            Log.d(TAG, message)
        }
    }

    fun logNodeSnapshot(snapshot: AccessibilityNodeSnapshot) {
        debug(snapshot.toLogLine())
    }

    fun logNodeScanSummary(result: AccessibilityNodeScanResult, eventTypeName: String) {
        debug(result.toSummaryLogLine(eventTypeName))
    }

    fun logNodeScanRootNull(eventTypeName: String) {
        debug("nodeScan root=null event=$eventTypeName")
    }
}
