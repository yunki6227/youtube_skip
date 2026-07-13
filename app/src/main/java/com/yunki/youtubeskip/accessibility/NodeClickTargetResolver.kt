package com.yunki.youtubeskip.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

enum class NodeClickTargetType {
    ACTION_CLICK,
    CLICKABLE_FLAG_ONLY,
}

data class NodeClickTargetCandidate(
    val ancestorDistance: Int,
    val isEnabled: Boolean,
    val isVisibleToUser: Boolean,
    val isClickable: Boolean,
    val supportsActionClick: Boolean,
)

data class NodeClickTargetChoice(
    val ancestorDistance: Int,
    val targetType: NodeClickTargetType,
)

data class ResolvedNodeClickTarget(
    val node: AccessibilityNodeInfo,
    val ancestorDistance: Int,
    val targetType: NodeClickTargetType,
    val className: String?,
    val boundsInScreen: String,
)

object NodeClickTargetPolicy {
    fun choose(
        candidates: List<NodeClickTargetCandidate>,
        maxAncestorDistance: Int = NodeClickTargetResolver.MAX_ANCESTOR_DISTANCE,
    ): NodeClickTargetChoice? {
        val boundedCandidates = candidates
            .filter { it.ancestorDistance <= maxAncestorDistance }
            .filter { it.isEnabled && it.isVisibleToUser }
            .sortedBy { it.ancestorDistance }

        val actionClickTarget = boundedCandidates.firstOrNull { it.supportsActionClick }
        if (actionClickTarget != null) {
            return NodeClickTargetChoice(
                ancestorDistance = actionClickTarget.ancestorDistance,
                targetType = NodeClickTargetType.ACTION_CLICK,
            )
        }

        val clickableFallback = boundedCandidates.firstOrNull { it.isClickable }
        return clickableFallback?.let {
            NodeClickTargetChoice(
                ancestorDistance = it.ancestorDistance,
                targetType = NodeClickTargetType.CLICKABLE_FLAG_ONLY,
            )
        }
    }
}

class NodeClickTargetResolver {
    fun resolve(candidate: AccessibilityNodeInfo): ResolvedNodeClickTarget? {
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        val targetCandidates = mutableListOf<NodeClickTargetCandidate>()

        var currentNode: AccessibilityNodeInfo? = candidate
        var ancestorDistance = 0
        while (currentNode != null && ancestorDistance <= MAX_ANCESTOR_DISTANCE) {
            nodes.add(currentNode)
            targetCandidates.add(currentNode.toTargetCandidate(ancestorDistance))
            currentNode = safeReadTarget { currentNode.parent }
            ancestorDistance += 1
        }

        val choice = NodeClickTargetPolicy.choose(targetCandidates) ?: return null
        val targetNode = nodes.getOrNull(choice.ancestorDistance) ?: return null
        val bounds = Rect()
        safeReadTarget {
            targetNode.getBoundsInScreen(bounds)
        }

        return ResolvedNodeClickTarget(
            node = targetNode,
            ancestorDistance = choice.ancestorDistance,
            targetType = choice.targetType,
            className = AccessibilityNodeSnapshot.normalizeMetadata(
                safeReadTarget { targetNode.className },
            ),
            boundsInScreen = "[${bounds.left},${bounds.top}][${bounds.right},${bounds.bottom}]",
        )
    }

    private fun AccessibilityNodeInfo.toTargetCandidate(
        ancestorDistance: Int,
    ): NodeClickTargetCandidate {
        return NodeClickTargetCandidate(
            ancestorDistance = ancestorDistance,
            isEnabled = safeReadTarget { isEnabled } == true,
            isVisibleToUser = safeReadTarget { isVisibleToUser } == true,
            isClickable = safeReadTarget { isClickable } == true,
            supportsActionClick = AccessibilityActionNames.supportsAction(
                node = this,
                actionId = AccessibilityNodeInfo.ACTION_CLICK,
            ),
        )
    }

    companion object {
        const val MAX_ANCESTOR_DISTANCE = 4
    }
}

private inline fun <T> safeReadTarget(block: () -> T): T? {
    return try {
        block()
    } catch (_: RuntimeException) {
        null
    }
}
