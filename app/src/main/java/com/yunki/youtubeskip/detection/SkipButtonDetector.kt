package com.yunki.youtubeskip.detection

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.yunki.youtubeskip.accessibility.AccessibilityNodeSnapshot
import com.yunki.youtubeskip.accessibility.NodeClickTargetResolver
import com.yunki.youtubeskip.accessibility.ResolvedNodeClickTarget

data class SkipButtonDetectionResult(
    val matchedLabel: String,
    val labelSource: SkipLabelSource,
    val candidateNode: AccessibilityNodeInfo,
    val clickTarget: ResolvedNodeClickTarget,
    val candidateClassName: String?,
    val ancestorDistance: Int,
    val targetBoundsInScreen: String,
) {
    val targetNode: AccessibilityNodeInfo
        get() = clickTarget.node
}

data class SkipButtonCandidateRanking(
    val priority: Int,
    val traversalOrder: Int,
    val targetSignature: String,
)

object SkipButtonCandidateDeduplicator {
    fun selectPreferredUnique(
        candidates: List<SkipButtonCandidateRanking>,
    ): List<SkipButtonCandidateRanking> {
        val selectedBySignature = linkedMapOf<String, SkipButtonCandidateRanking>()
        candidates
            .sortedWith(compareBy<SkipButtonCandidateRanking> { it.priority }.thenBy { it.traversalOrder })
            .forEach { candidate ->
                selectedBySignature.putIfAbsent(candidate.targetSignature, candidate)
            }

        return selectedBySignature.values.toList()
    }
}

class SkipButtonDetector(
    private val clickTargetResolver: NodeClickTargetResolver = NodeClickTargetResolver(),
    private val maxNodeCount: Int = DEFAULT_MAX_NODE_COUNT,
    private val maxDepth: Int = DEFAULT_MAX_DEPTH,
) {
    fun detect(root: AccessibilityNodeInfo): SkipButtonDetectionResult? {
        val candidates = findCandidates(root)
        val preferredRanking = SkipButtonCandidateDeduplicator
            .selectPreferredUnique(candidates.map { it.ranking })
            .firstOrNull() ?: return null

        return candidates.firstOrNull { it.ranking == preferredRanking }?.result
    }

    fun containsSemanticSkipCandidate(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<NodeWithDepth>()
        queue.add(NodeWithDepth(root, depth = 0))

        var visitedNodeCount = 0
        while (queue.isNotEmpty() && visitedNodeCount < maxNodeCount) {
            val current = queue.removeFirst()
            if (current.depth > maxDepth) {
                continue
            }

            visitedNodeCount += 1
            val node = current.node
            if (isCandidateNodeAvailable(node) &&
                SkipButtonLabelMatcher.match(
                    text = safeDetectorRead { node.text },
                    contentDescription = safeDetectorRead { node.contentDescription },
                ) != null
            ) {
                return true
            }

            enqueueChildren(queue, node, current.depth)
        }

        return false
    }

    private fun findCandidates(root: AccessibilityNodeInfo): List<DetectionCandidate> {
        val candidates = mutableListOf<DetectionCandidate>()
        val queue = ArrayDeque<NodeWithDepth>()
        queue.add(NodeWithDepth(root, depth = 0))

        var visitedNodeCount = 0
        var traversalOrder = 0

        while (queue.isNotEmpty() && visitedNodeCount < maxNodeCount) {
            val current = queue.removeFirst()
            if (current.depth > maxDepth) {
                continue
            }

            visitedNodeCount += 1
            val node = current.node
            if (!isCandidateNodeAvailable(node)) {
                enqueueChildren(queue, node, current.depth)
                continue
            }

            val labelMatch = SkipButtonLabelMatcher.match(
                text = safeDetectorRead { node.text },
                contentDescription = safeDetectorRead { node.contentDescription },
            )

            if (labelMatch != null) {
                val clickTarget = clickTargetResolver.resolve(node)
                if (clickTarget != null) {
                    candidates.add(
                        DetectionCandidate(
                            ranking = SkipButtonCandidateRanking(
                                priority = labelMatch.priority,
                                traversalOrder = traversalOrder,
                                targetSignature = targetSignature(clickTarget),
                            ),
                            result = SkipButtonDetectionResult(
                                matchedLabel = labelMatch.normalizedLabel,
                                labelSource = labelMatch.labelSource,
                                candidateNode = node,
                                clickTarget = clickTarget,
                                candidateClassName = AccessibilityNodeSnapshot.normalizeMetadata(
                                    safeDetectorRead { node.className },
                                ),
                                ancestorDistance = clickTarget.ancestorDistance,
                                targetBoundsInScreen = clickTarget.boundsInScreen,
                            ),
                        ),
                    )
                    traversalOrder += 1
                }
            }

            enqueueChildren(queue, node, current.depth)
        }

        return candidates
    }

    private fun enqueueChildren(
        queue: ArrayDeque<NodeWithDepth>,
        node: AccessibilityNodeInfo,
        depth: Int,
    ) {
        if (depth >= maxDepth) {
            return
        }

        val childCount = safeDetectorRead { node.childCount } ?: 0
        for (index in 0 until childCount) {
            if (queue.size >= maxNodeCount) {
                break
            }

            val child = safeDetectorRead { node.getChild(index) } ?: continue
            queue.addLast(NodeWithDepth(child, depth + 1))
        }
    }

    private fun isCandidateNodeAvailable(node: AccessibilityNodeInfo): Boolean {
        return safeDetectorRead { node.isEnabled } == true &&
            safeDetectorRead { node.isVisibleToUser } == true &&
            safeDetectorRead { node.isEditable } != true
    }

    private fun targetSignature(clickTarget: ResolvedNodeClickTarget): String {
        return "${clickTarget.className.orEmpty()}|${clickTarget.boundsInScreen}"
    }

    private data class NodeWithDepth(
        val node: AccessibilityNodeInfo,
        val depth: Int,
    )

    private data class DetectionCandidate(
        val ranking: SkipButtonCandidateRanking,
        val result: SkipButtonDetectionResult,
    )

    private companion object {
        const val DEFAULT_MAX_NODE_COUNT = 200
        const val DEFAULT_MAX_DEPTH = 20
    }
}

private inline fun <T> safeDetectorRead(block: () -> T): T? {
    return try {
        block()
    } catch (_: RuntimeException) {
        null
    }
}
