package com.yunki.youtubeskip.accessibility

class AccessibilityEventProcessingDebounce(
    private val debounceWindowMillis: Long = DEFAULT_DEBOUNCE_WINDOW_MILLIS,
) {
    private var lastProcessedAtMillis: Long? = null

    fun shouldProcess(nowMillis: Long): Boolean {
        val lastProcessedAt = lastProcessedAtMillis
        if (lastProcessedAt != null && nowMillis - lastProcessedAt < debounceWindowMillis) {
            return false
        }

        lastProcessedAtMillis = nowMillis
        return true
    }

    companion object {
        const val DEFAULT_DEBOUNCE_WINDOW_MILLIS = 350L
    }
}

class SuccessfulClickCooldown(
    private val cooldownMillis: Long = DEFAULT_COOLDOWN_MILLIS,
    private val suppressionLogWindowMillis: Long = DEFAULT_SUPPRESSION_LOG_WINDOW_MILLIS,
) {
    private var lastSuccessfulClickAtMillis: Long? = null
    private var lastSuppressionLogAtMillis: Long? = null

    fun recordSuccessfulClick(nowMillis: Long) {
        lastSuccessfulClickAtMillis = nowMillis
        lastSuppressionLogAtMillis = null
    }

    fun isActive(nowMillis: Long): Boolean {
        val lastSuccessfulClickAt = lastSuccessfulClickAtMillis ?: return false
        return nowMillis - lastSuccessfulClickAt < cooldownMillis
    }

    fun shouldLogSuppression(nowMillis: Long): Boolean {
        if (!isActive(nowMillis)) {
            return false
        }

        val lastSuppressionLogAt = lastSuppressionLogAtMillis
        if (lastSuppressionLogAt != null &&
            nowMillis - lastSuppressionLogAt < suppressionLogWindowMillis
        ) {
            return false
        }

        lastSuppressionLogAtMillis = nowMillis
        return true
    }

    companion object {
        const val DEFAULT_COOLDOWN_MILLIS = 3_000L
        const val DEFAULT_SUPPRESSION_LOG_WINDOW_MILLIS = 1_000L
    }
}

enum class SkipVerificationResult(
    val logName: String,
) {
    CANDIDATE_DISAPPEARED("candidate_disappeared"),
    CANDIDATE_STILL_PRESENT("candidate_still_present"),
    ROOT_UNAVAILABLE("root_unavailable"),
}

object SkipVerificationPolicy {
    fun resultFor(
        rootAvailable: Boolean,
        candidateStillPresent: Boolean,
    ): SkipVerificationResult {
        return when {
            !rootAvailable -> SkipVerificationResult.ROOT_UNAVAILABLE
            candidateStillPresent -> SkipVerificationResult.CANDIDATE_STILL_PRESENT
            else -> SkipVerificationResult.CANDIDATE_DISAPPEARED
        }
    }
}
