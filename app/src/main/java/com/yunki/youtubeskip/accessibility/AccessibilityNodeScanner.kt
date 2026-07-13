package com.yunki.youtubeskip.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.yunki.youtubeskip.detection.SkipCandidateMatcher

class AccessibilityNodeScanner(
    private val maxNodeCount: Int = DEFAULT_MAX_NODE_COUNT,
    private val maxDepth: Int = DEFAULT_MAX_DEPTH,
) {
    fun scan(root: AccessibilityNodeInfo): AccessibilityNodeScanResult {
        val snapshots = mutableListOf<AccessibilityNodeSnapshot>()
        val skipCandidateDiagnostics = mutableListOf<SkipCandidateDiagnostic>()
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

            skipCandidateDiagnostic(current.node, current.depth)?.let(skipCandidateDiagnostics::add)

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
            skipCandidateDiagnostics = skipCandidateDiagnostics,
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

    private fun skipCandidateDiagnostic(
        node: AccessibilityNodeInfo,
        depth: Int,
    ): SkipCandidateDiagnostic? {
        val match = SkipCandidateMatcher.match(
            text = safeRead { node.text },
            contentDescription = safeRead { node.contentDescription },
        ) ?: return null

        val ancestors = mutableListOf<SkipAncestorDiagnostic>()
        var currentNode: AccessibilityNodeInfo? = node
        var levelIndex = 0
        while (currentNode != null && levelIndex <= MAX_ANCESTOR_COUNT) {
            ancestors.add(currentNode.toSkipAncestorDiagnostic(levelIndex))
            currentNode = safeRead { currentNode.parent }
            levelIndex += 1
        }

        return SkipCandidateDiagnostic.create(
            match = match,
            depth = depth,
            ancestors = ancestors,
        )
    }

    private fun AccessibilityNodeInfo.toSkipAncestorDiagnostic(levelIndex: Int): SkipAncestorDiagnostic {
        val bounds = Rect()
        safeRead {
            getBoundsInScreen(bounds)
        }

        val actionIds = actionIds()
        return SkipAncestorDiagnostic(
            level = levelName(levelIndex),
            text = AccessibilityNodeSnapshot.normalizeLogText(safeRead { text }),
            contentDescription = AccessibilityNodeSnapshot.normalizeLogText(
                safeRead { contentDescription },
            ),
            className = normalizedClassName(this),
            viewIdResourceName = AccessibilityNodeSnapshot.normalizeMetadata(
                safeRead { viewIdResourceName },
            ),
            isClickable = safeRead { isClickable } == true,
            isEnabled = safeRead { isEnabled } == true,
            isFocusable = safeRead { isFocusable } == true,
            isVisibleToUser = safeRead { isVisibleToUser } == true,
            boundsInScreen = "[${bounds.left},${bounds.top}][${bounds.right},${bounds.bottom}]",
            actionIds = actionIds,
            actionNames = AccessibilityActionNames.namesFor(actionIds),
        )
    }

    @Suppress("DEPRECATION")
    private fun AccessibilityNodeInfo.actionIds(): List<Int> {
        return AccessibilityActionNames.actionIdsFor(this)
    }

    private fun levelName(levelIndex: Int): String {
        return if (levelIndex == 0) {
            SkipCandidateDiagnostic.CANDIDATE_LEVEL
        } else {
            "parent-$levelIndex"
        }
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
        const val MAX_ANCESTOR_COUNT = 6
    }
}

private inline fun <T> safeRead(block: () -> T): T? {
    return try {
        block()
    } catch (_: RuntimeException) {
        null
    }
}
