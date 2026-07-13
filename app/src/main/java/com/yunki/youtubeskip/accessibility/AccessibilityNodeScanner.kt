package com.yunki.youtubeskip.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityNodeScanner(
    private val maxNodeCount: Int = DEFAULT_MAX_NODE_COUNT,
    private val maxDepth: Int = DEFAULT_MAX_DEPTH,
) {
    fun scan(root: AccessibilityNodeInfo): AccessibilityNodeScanResult {
        val snapshots = mutableListOf<AccessibilityNodeSnapshot>()
        val queue = ArrayDeque<NodeWithDepth>()
        queue.add(NodeWithDepth(root, depth = 0))

        var visitedNodeCount = 0
        var truncated = false

        while (queue.isNotEmpty()) {
            if (visitedNodeCount >= maxNodeCount) {
                truncated = true
                break
            }

            val current = queue.removeFirst()
            if (current.depth > maxDepth) {
                truncated = true
                continue
            }

            visitedNodeCount += 1
            if (isInputNode(current.node)) {
                continue
            }

            snapshotNode(current.node, current.depth)?.let { snapshot ->
                if (snapshot.hasUsefulField()) {
                    snapshots.add(snapshot)
                }
            }

            val childCount = safeRead { current.node.childCount } ?: 0
            if (current.depth >= maxDepth) {
                if (childCount > 0) {
                    truncated = true
                }
                continue
            }

            for (index in 0 until childCount) {
                if (visitedNodeCount + queue.size >= maxNodeCount) {
                    truncated = true
                    break
                }

                val child = safeRead { current.node.getChild(index) } ?: continue
                queue.addLast(NodeWithDepth(child, current.depth + 1))
            }
        }

        if (queue.isNotEmpty()) {
            truncated = true
        }

        return AccessibilityNodeScanResult(
            snapshots = snapshots,
            visitedNodeCount = visitedNodeCount,
            truncated = truncated,
        )
    }

    private fun snapshotNode(
        node: AccessibilityNodeInfo,
        depth: Int,
    ): AccessibilityNodeSnapshot? {
        val className = normalizedClassName(node)

        if (safeRead { node.isVisibleToUser } == false) {
            return null
        }

        val bounds = Rect()
        safeRead {
            node.getBoundsInScreen(bounds)
        }

        return AccessibilityNodeSnapshot(
            depth = depth,
            text = AccessibilityNodeSnapshot.normalizeLogText(safeRead { node.text }),
            contentDescription = AccessibilityNodeSnapshot.normalizeLogText(
                safeRead { node.contentDescription },
            ),
            className = className,
            viewIdResourceName = AccessibilityNodeSnapshot.normalizeMetadata(
                safeRead { node.viewIdResourceName },
            ),
            isClickable = safeRead { node.isClickable } == true,
            isEnabled = safeRead { node.isEnabled } == true,
            boundsInScreen = "[${bounds.left},${bounds.top}][${bounds.right},${bounds.bottom}]",
        )
    }

    private fun isInputNode(node: AccessibilityNodeInfo): Boolean {
        return safeRead { node.isEditable } == true ||
            normalizedClassName(node)?.contains("EditText", ignoreCase = true) == true
    }

    private fun normalizedClassName(node: AccessibilityNodeInfo): String? {
        return AccessibilityNodeSnapshot.normalizeMetadata(
            safeRead { node.className },
        )
    }

    private data class NodeWithDepth(
        val node: AccessibilityNodeInfo,
        val depth: Int,
    )

    private companion object {
        const val DEFAULT_MAX_NODE_COUNT = 200
        const val DEFAULT_MAX_DEPTH = 20
    }
}

private inline fun <T> safeRead(block: () -> T): T? {
    return try {
        block()
    } catch (_: RuntimeException) {
        null
    }
}
