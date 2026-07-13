package com.yunki.youtubeskip.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SkipButtonLabelMatcherTest {
    @Test
    fun prioritizesContentDescriptionSkipAdBeforeOtherLabels() {
        val match = SkipButtonLabelMatcher.match(
            text = "Skip ads",
            contentDescription = "Skip ad",
        )

        assertEquals(SkipLabelSource.CONTENT_DESCRIPTION, match?.labelSource)
        assertEquals("skip ad", match?.normalizedLabel)
        assertEquals(1, match?.priority)
    }

    @Test
    fun prioritizesContentDescriptionSkipAdsBeforeTextSkipAd() {
        val match = SkipButtonLabelMatcher.match(
            text = "Skip ad",
            contentDescription = "Skip ads",
        )

        assertEquals(SkipLabelSource.CONTENT_DESCRIPTION, match?.labelSource)
        assertEquals("skip ads", match?.normalizedLabel)
        assertEquals(2, match?.priority)
    }

    @Test
    fun matchesTextSkipAdAndSkipAdsWhenSemanticDescriptionIsAbsent() {
        assertEquals(
            3,
            SkipButtonLabelMatcher.match(text = "  SKIP\tad ", contentDescription = null)?.priority,
        )
        assertEquals(
            4,
            SkipButtonLabelMatcher.match(text = "Skip ads", contentDescription = null)?.priority,
        )
    }

    @Test
    fun rejectsPlainSkipAndVisibleSymbolVariantsForProductionClicking() {
        assertNull(SkipButtonLabelMatcher.match(text = "Skip", contentDescription = null))
        assertNull(SkipButtonLabelMatcher.match(text = "Skip >|", contentDescription = null))
        assertNull(SkipButtonLabelMatcher.match(text = null, contentDescription = "Skip ▶|"))
    }

    @Test
    fun rejectsLooseSubstringMatches() {
        assertNull(SkipButtonLabelMatcher.match(text = "Tap to skip ad", contentDescription = null))
        assertNull(SkipButtonLabelMatcher.match(text = null, contentDescription = "Skip intro"))
    }
}
