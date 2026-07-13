package com.yunki.youtubeskip.accessibility

import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityActionNames {
    const val ACTION_CLICK_ID = AccessibilityNodeInfo.ACTION_CLICK
    const val ACTION_DISMISS_ID = AccessibilityNodeInfo.ACTION_DISMISS
    const val ACTION_FOCUS_ID = AccessibilityNodeInfo.ACTION_FOCUS

    private val knownActionNames = linkedMapOf(
        AccessibilityNodeInfo.ACTION_FOCUS to "ACTION_FOCUS",
        AccessibilityNodeInfo.ACTION_CLEAR_FOCUS to "ACTION_CLEAR_FOCUS",
        AccessibilityNodeInfo.ACTION_SELECT to "ACTION_SELECT",
        AccessibilityNodeInfo.ACTION_CLEAR_SELECTION to "ACTION_CLEAR_SELECTION",
        AccessibilityNodeInfo.ACTION_CLICK to "ACTION_CLICK",
        AccessibilityNodeInfo.ACTION_LONG_CLICK to "ACTION_LONG_CLICK",
        AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS to "ACTION_ACCESSIBILITY_FOCUS",
        AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS to "ACTION_CLEAR_ACCESSIBILITY_FOCUS",
        AccessibilityNodeInfo.ACTION_NEXT_AT_MOVEMENT_GRANULARITY to "ACTION_NEXT_AT_MOVEMENT_GRANULARITY",
        AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY to "ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY",
        AccessibilityNodeInfo.ACTION_NEXT_HTML_ELEMENT to "ACTION_NEXT_HTML_ELEMENT",
        AccessibilityNodeInfo.ACTION_PREVIOUS_HTML_ELEMENT to "ACTION_PREVIOUS_HTML_ELEMENT",
        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD to "ACTION_SCROLL_FORWARD",
        AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD to "ACTION_SCROLL_BACKWARD",
        AccessibilityNodeInfo.ACTION_COPY to "ACTION_COPY",
        AccessibilityNodeInfo.ACTION_PASTE to "ACTION_PASTE",
        AccessibilityNodeInfo.ACTION_CUT to "ACTION_CUT",
        AccessibilityNodeInfo.ACTION_SET_SELECTION to "ACTION_SET_SELECTION",
        AccessibilityNodeInfo.ACTION_EXPAND to "ACTION_EXPAND",
        AccessibilityNodeInfo.ACTION_COLLAPSE to "ACTION_COLLAPSE",
        AccessibilityNodeInfo.ACTION_DISMISS to "ACTION_DISMISS",
        AccessibilityNodeInfo.ACTION_SET_TEXT to "ACTION_SET_TEXT",
    )

    fun nameFor(actionId: Int): String {
        return knownActionNames[actionId] ?: "ACTION_$actionId"
    }

    fun namesFor(actionIds: List<Int>): List<String> {
        return actionIds
            .distinct()
            .sorted()
            .map(::nameFor)
    }

    fun actionIdsFromLegacyBitmask(actions: Int): List<Int> {
        return knownActionNames.keys.filter { actionId ->
            actions and actionId == actionId
        }
    }

    fun supportsAction(
        node: AccessibilityNodeInfo,
        actionId: Int,
    ): Boolean {
        return actionIdsFor(node).contains(actionId)
    }

    @Suppress("DEPRECATION")
    fun actionIdsFor(node: AccessibilityNodeInfo): List<Int> {
        val actionListIds = safeActionRead {
            node.actionList.map { action -> action.id }
        }.orEmpty()
        val legacyActionIds = actionIdsFromLegacyBitmask(
            safeActionRead { node.actions } ?: 0,
        )

        return (actionListIds + legacyActionIds)
            .distinct()
            .sorted()
    }
}

private inline fun <T> safeActionRead(block: () -> T): T? {
    return try {
        block()
    } catch (_: RuntimeException) {
        null
    }
}
