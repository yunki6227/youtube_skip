package com.yunki.youtubeskip.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SkipCandidateMatcherTest {
    @Test
    fun matchesTextSkip() {
        val match = SkipCandidateMatcher.match(text = "Skip", contentDescription = null)

        assertEquals(SkipLabelSource.TEXT, match?.labelSource)
        assertEquals("skip", match?.normalizedLabel)
    }

    @Test
    fun matchesTextSkipAdWithCaseAndWhitespaceNormalization() {
        val match = SkipCandidateMatcher.match(text = "  SKIP\tad  ", contentDescription = null)

        assertEquals(SkipLabelSource.TEXT, match?.labelSource)
        assertEquals("skip ad", match?.normalizedLabel)
    }

    @Test
    fun matchesContentDescriptionSkipAds() {
        val match = SkipCandidateMatcher.match(text = null, contentDescription = "Skip ads")

        assertEquals(SkipLabelSource.CONTENT_DESCRIPTION, match?.labelSource)
        assertEquals("skip ads", match?.normalizedLabel)
    }

    @Test
    fun matchesVisibleSymbolVariants() {
        assertEquals(
            "skip >|",
            SkipCandidateMatcher.match(text = "Skip>|", contentDescription = null)?.normalizedLabel,
        )
        assertEquals(
            "skip ▶|",
            SkipCandidateMatcher.match(text = "Skip ▶ |", contentDescription = null)?.normalizedLabel,
        )
        assertEquals(
            "skip ▷|",
            SkipCandidateMatcher.match(text = "Skip ▷|", contentDescription = null)?.normalizedLabel,
        )
    }

    @Test
    fun rejectsLooseSubstringMatches() {
        assertNull(SkipCandidateMatcher.match(text = "Skip intro", contentDescription = null))
        assertNull(SkipCandidateMatcher.match(text = "tap to skip ad", contentDescription = null))
    }

    @Test
    fun rejectsMalformedOrUnrelatedLabels() {
        assertNull(SkipCandidateMatcher.match(text = "", contentDescription = ""))
        assertNull(SkipCandidateMatcher.match(text = "Skip this ad", contentDescription = null))
        assertNull(SkipCandidateMatcher.match(text = null, contentDescription = "Skip"))
    }
}
