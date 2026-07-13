package com.yunki.youtubeskip.detection

import java.util.Locale

data class SkipButtonLabelMatch(
    val labelSource: SkipLabelSource,
    val normalizedLabel: String,
    val priority: Int,
)

object SkipButtonLabelMatcher {
    fun match(
        text: CharSequence?,
        contentDescription: CharSequence?,
    ): SkipButtonLabelMatch? {
        val normalizedDescription = normalizeLabel(contentDescription)
        when (normalizedDescription) {
            "skip ad" -> return SkipButtonLabelMatch(
                labelSource = SkipLabelSource.CONTENT_DESCRIPTION,
                normalizedLabel = normalizedDescription,
                priority = 1,
            )

            "skip ads" -> return SkipButtonLabelMatch(
                labelSource = SkipLabelSource.CONTENT_DESCRIPTION,
                normalizedLabel = normalizedDescription,
                priority = 2,
            )
        }

        val normalizedText = normalizeLabel(text)
        return when (normalizedText) {
            "skip ad" -> SkipButtonLabelMatch(
                labelSource = SkipLabelSource.TEXT,
                normalizedLabel = normalizedText,
                priority = 3,
            )

            "skip ads" -> SkipButtonLabelMatch(
                labelSource = SkipLabelSource.TEXT,
                normalizedLabel = normalizedText,
                priority = 4,
            )

            else -> null
        }
    }

    fun normalizeLabel(value: CharSequence?): String? {
        return value
            ?.toString()
            ?.replace(WHITESPACE_REGEX, " ")
            ?.trim()
            ?.lowercase(Locale.US)
            ?.takeIf { it.isNotEmpty() }
    }

    private val WHITESPACE_REGEX = Regex("\\s+")
}
