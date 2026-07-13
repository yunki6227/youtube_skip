package com.yunki.youtubeskip.accessibility

import com.yunki.youtubeskip.detection.SkipCandidateMatch

enum class ClickTargetClassification {
    DIRECT_CLICK_ACTION,
    CLICKABLE_FLAG_ONLY,
    NON_CLICKABLE,
    DISABLED,
    NOT_VISIBLE,
}

enum class SkipTargetRecommendationReason(
    val logName: String,
) {
    CANDIDATE_ENABLED_VISIBLE_CLICK_ACTION("candidate_enabled_visible_click_action"),
    NEAREST_ENABLED_VISIBLE_CLICK_ACTION("nearest_enabled_visible_click_action"),
    NEAREST_ENABLED_VISIBLE_CLICKABLE("nearest_enabled_visible_clickable"),
    NONE("none"),
}

data class SkipAncestorDiagnostic(
    val level: String,
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val viewIdResourceName: String?,
    val isClickable: Boolean,
    val isEnabled: Boolean,
    val isFocusable: Boolean,
    val isVisibleToUser: Boolean,
    val boundsInScreen: String,
    val actionIds: List<Int>,
    val actionNames: List<String>,
) {
    val supportsClick: Boolean = actionIds.contains(AccessibilityActionNames.ACTION_CLICK_ID)
    val supportsDismiss: Boolean = actionIds.contains(AccessibilityActionNames.ACTION_DISMISS_ID)
    val supportsFocus: Boolean = actionIds.contains(AccessibilityActionNames.ACTION_FOCUS_ID)
    val classification: ClickTargetClassification = classifyClickTarget(
        isEnabled = isEnabled,
        isVisibleToUser = isVisibleToUser,
        isClickable = isClickable,
        supportsClick = supportsClick,
    )

    fun toLogLine(): String {
        return "skipAncestor level=$level " +
            "class=${className ?: "null"} " +
            "text=${text.toLogValue()} " +
            "desc=${contentDescription.toLogValue()} " +
            "clickable=$isClickable " +
            "enabled=$isEnabled " +
            "focusable=$isFocusable " +
            "visible=$isVisibleToUser " +
            "classification=$classification " +
            "supportsClick=$supportsClick " +
            "supportsDismiss=$supportsDismiss " +
            "supportsFocus=$supportsFocus " +
            "actionIds=${actionIds.toCompactList()} " +
            "actionNames=${actionNames.toCompactList()} " +
            "viewId=${viewIdResourceName ?: "null"} " +
            "bounds=$boundsInScreen"
    }

    companion object {
        fun classifyClickTarget(
            isEnabled: Boolean,
            isVisibleToUser: Boolean,
            isClickable: Boolean,
            supportsClick: Boolean,
        ): ClickTargetClassification {
            return when {
                !isEnabled -> ClickTargetClassification.DISABLED
                !isVisibleToUser -> ClickTargetClassification.NOT_VISIBLE
                supportsClick -> ClickTargetClassification.DIRECT_CLICK_ACTION
                isClickable -> ClickTargetClassification.CLICKABLE_FLAG_ONLY
                else -> ClickTargetClassification.NON_CLICKABLE
            }
        }
    }
}

data class SkipCandidateDiagnostic(
    val match: SkipCandidateMatch,
    val depth: Int,
    val ancestors: List<SkipAncestorDiagnostic>,
    val firstClickableAncestorLevel: String?,
    val firstClickActionAncestorLevel: String?,
    val recommendedDiagnosticTargetLevel: String?,
    val recommendationReason: SkipTargetRecommendationReason,
) {
    fun candidateLogLine(): String {
        return "skipCandidate " +
            "labelSource=${match.labelSource.logName} " +
            "normalized=${match.normalizedLabel.toLogValue()} " +
            "depth=$depth " +
            "bounds=${ancestors.firstOrNull()?.boundsInScreen ?: "null"}"
    }

    fun targetLogLine(): String {
        return "skipTarget " +
            "recommended=${recommendedDiagnosticTargetLevel ?: "none"} " +
            "reason=${recommendationReason.logName} " +
            "firstClickableAncestor=${firstClickableAncestorLevel ?: "none"} " +
            "firstClickActionAncestor=${firstClickActionAncestorLevel ?: "none"}"
    }

    fun signature(): String {
        val candidate = ancestors.firstOrNull()
        return listOf(
            match.labelSource.logName,
            match.normalizedLabel,
            candidate?.boundsInScreen.orEmpty(),
            candidate?.className.orEmpty(),
            recommendedDiagnosticTargetLevel.orEmpty(),
        ).joinToString("|")
    }

    companion object {
        fun create(
            match: SkipCandidateMatch,
            depth: Int,
            ancestors: List<SkipAncestorDiagnostic>,
        ): SkipCandidateDiagnostic {
            val firstClickAction = ancestors.firstOrNull {
                it.isEnabled && it.isVisibleToUser && it.supportsClick
            }
            val firstClickable = ancestors.firstOrNull {
                it.isEnabled && it.isVisibleToUser && it.isClickable
            }
            val recommended = firstClickAction ?: firstClickable
            val reason = when {
                firstClickAction?.level == CANDIDATE_LEVEL ->
                    SkipTargetRecommendationReason.CANDIDATE_ENABLED_VISIBLE_CLICK_ACTION
                firstClickAction != null ->
                    SkipTargetRecommendationReason.NEAREST_ENABLED_VISIBLE_CLICK_ACTION
                firstClickable != null ->
                    SkipTargetRecommendationReason.NEAREST_ENABLED_VISIBLE_CLICKABLE
                else ->
                    SkipTargetRecommendationReason.NONE
            }

            return SkipCandidateDiagnostic(
                match = match,
                depth = depth,
                ancestors = ancestors,
                firstClickableAncestorLevel = firstClickable?.level,
                firstClickActionAncestorLevel = firstClickAction?.level,
                recommendedDiagnosticTargetLevel = recommended?.level,
                recommendationReason = reason,
            )
        }

        const val CANDIDATE_LEVEL = "candidate"
    }
}

class SkipCandidateDiagnosticDeduplicator(
    private val suppressionWindowMillis: Long = DEFAULT_SUPPRESSION_WINDOW_MILLIS,
) {
    private val lastLoggedAtMillisBySignature = mutableMapOf<String, Long>()

    fun shouldLog(
        signature: String,
        nowMillis: Long,
    ): Boolean {
        val lastLoggedAtMillis = lastLoggedAtMillisBySignature[signature]
        if (lastLoggedAtMillis != null && nowMillis - lastLoggedAtMillis < suppressionWindowMillis) {
            return false
        }

        lastLoggedAtMillisBySignature[signature] = nowMillis
        prune(nowMillis)
        return true
    }

    private fun prune(nowMillis: Long) {
        lastLoggedAtMillisBySignature.entries.removeAll { (_, lastLoggedAtMillis) ->
            nowMillis - lastLoggedAtMillis >= suppressionWindowMillis
        }
    }

    private companion object {
        const val DEFAULT_SUPPRESSION_WINDOW_MILLIS = 2_000L
    }
}

private fun String?.toLogValue(): String {
    return if (this == null) {
        "null"
    } else {
        "\"${replace("\"", "'")}\""
    }
}

private fun <T> List<T>.toCompactList(): String {
    return joinToString(prefix = "[", postfix = "]", separator = ",")
}
