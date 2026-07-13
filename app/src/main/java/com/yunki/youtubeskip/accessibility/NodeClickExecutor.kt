package com.yunki.youtubeskip.accessibility

import android.view.accessibility.AccessibilityNodeInfo

enum class NodeClickExecutionStatus(
    val logName: String,
) {
    SUCCESS("success"),
    ACTION_RETURNED_FALSE("action_returned_false"),
    TARGET_STALE_OR_UNAVAILABLE("target_stale_or_unavailable"),
    TARGET_NO_LONGER_ENABLED_VISIBLE("target_no_longer_enabled_visible"),
    TARGET_ACTION_UNAVAILABLE("target_action_unavailable"),
    EXCEPTION("exception"),
}

data class NodeClickExecutionResult(
    val status: NodeClickExecutionStatus,
    val exceptionClassName: String? = null,
) {
    val isSuccess: Boolean = status == NodeClickExecutionStatus.SUCCESS
}

class NodeClickExecutor {
    fun click(target: AccessibilityNodeInfo): NodeClickExecutionResult {
        return try {
            val isEnabled = safeClickRead { target.isEnabled }
                ?: return NodeClickExecutionResult(NodeClickExecutionStatus.TARGET_STALE_OR_UNAVAILABLE)
            val isVisible = safeClickRead { target.isVisibleToUser }
                ?: return NodeClickExecutionResult(NodeClickExecutionStatus.TARGET_STALE_OR_UNAVAILABLE)

            if (!isEnabled || !isVisible) {
                return NodeClickExecutionResult(
                    NodeClickExecutionStatus.TARGET_NO_LONGER_ENABLED_VISIBLE,
                )
            }

            if (!AccessibilityActionNames.supportsAction(target, AccessibilityNodeInfo.ACTION_CLICK)) {
                return NodeClickExecutionResult(NodeClickExecutionStatus.TARGET_ACTION_UNAVAILABLE)
            }

            if (target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                NodeClickExecutionResult(NodeClickExecutionStatus.SUCCESS)
            } else {
                NodeClickExecutionResult(NodeClickExecutionStatus.ACTION_RETURNED_FALSE)
            }
        } catch (exception: RuntimeException) {
            NodeClickExecutionResult(
                status = NodeClickExecutionStatus.EXCEPTION,
                exceptionClassName = exception::class.java.simpleName,
            )
        }
    }
}

private inline fun <T> safeClickRead(block: () -> T): T? {
    return try {
        block()
    } catch (_: RuntimeException) {
        null
    }
}
