package com.yunki.youtubeskip.accessibility

data class AccessibilityNodeSnapshot(
    val depth: Int,
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val viewIdResourceName: String?,
    val isClickable: Boolean,
    val isEnabled: Boolean,
    val boundsInScreen: String,
) {
    fun hasUsefulField(): Boolean {
        return text != null ||
            contentDescription != null ||
            !viewIdResourceName.isNullOrBlank() ||
            isClickable
    }

    fun toLogLine(): String {
        return "node depth=$depth " +
            "text=${text.toLogValue()} " +
            "desc=${contentDescription.toLogValue()} " +
            "class=${className ?: "null"} " +
            "viewId=${viewIdResourceName ?: "null"} " +
            "clickable=$isClickable " +
            "enabled=$isEnabled " +
            "bounds=$boundsInScreen"
    }

    companion object {
        const val MAX_LOG_FIELD_LENGTH = 80

        fun normalizeLogText(value: CharSequence?): String? {
            return normalizeMetadata(value)
                ?.let { normalized ->
                    if (normalized.length <= MAX_LOG_FIELD_LENGTH) {
                        normalized
                    } else {
                        normalized.take(MAX_LOG_FIELD_LENGTH)
                    }
                }
        }

        fun normalizeMetadata(value: CharSequence?): String? {
            return value
                ?.toString()
                ?.replace(WHITESPACE_REGEX, " ")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }

        private val WHITESPACE_REGEX = Regex("\\s+")
    }
}

data class AccessibilityNodeScanResult(
    val snapshots: List<AccessibilityNodeSnapshot>,
    val visitedNodeCount: Int,
    val truncated: Boolean,
) {
    fun toSummaryLogLine(eventTypeName: String): String {
        return "nodeScan count=${snapshots.size} " +
            "visited=$visitedNodeCount " +
            "truncated=$truncated " +
            "event=$eventTypeName"
    }
}

private fun String?.toLogValue(): String {
    return if (this == null) {
        "null"
    } else {
        "\"${replace("\"", "'")}\""
    }
}
