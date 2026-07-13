package com.yunki.youtubeskip.detection

import java.util.Locale

enum class SkipLabelSource(
    val logName: String,
) {
    TEXT("text"),
    CONTENT_DESCRIPTION("contentDescription"),
}

data class SkipCandidateMatch(
    val labelSource: SkipLabelSource,
    val normalizedLabel: String,
)

object SkipCandidateMatcher {
    private val textLabels = setOf(
        "skip",
        "skip ad",
        "skip ads",
        "skip >|",
        "skip ▶|",
        "skip ▷|",
    )

    private val contentDescriptionLabels = setOf(
        "skip ad",
        "skip ads",
        "skip >|",
        "skip ▶|",
        "skip ▷|",
    )

    fun match(
        text: CharSequence?,
        contentDescription: CharSequence?,
    ): SkipCandidateMatch? {
        val normalizedText = normalizeLabel(text)
        if (normalizedText != null && normalizedText in textLabels) {
            return SkipCandidateMatch(
                labelSource = SkipLabelSource.TEXT,
                normalizedLabel = normalizedText,
            )
        }

        val normalizedDescription = normalizeLabel(contentDescription)
        if (normalizedDescription != null && normalizedDescription in contentDescriptionLabels) {
            return SkipCandidateMatch(
                labelSource = SkipLabelSource.CONTENT_DESCRIPTION,
                normalizedLabel = normalizedDescription,
            )
        }

        return null
    }

    fun normalizeLabel(value: CharSequence?): String? {
        return value
            ?.toString()
            ?.replace(WHITESPACE_REGEX, " ")
            ?.trim()
            ?.lowercase(Locale.US)
            ?.replace(SKIP_SYMBOL_REGEX, "skip $1|")
            ?.takeIf { it.isNotEmpty() }
    }

    private val WHITESPACE_REGEX = Regex("\\s+")
    private val SKIP_SYMBOL_REGEX = Regex("^skip\\s*([>▶▷])\\s*\\|$")
}
