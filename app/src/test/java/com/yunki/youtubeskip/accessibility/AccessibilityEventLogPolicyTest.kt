package com.yunki.youtubeskip.accessibility

import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class AccessibilityEventLogPolicyTest {
    @Test
    fun supportedEventTypesHaveReadableNames() {
        assertEquals(
            "WINDOW_CONTENT_CHANGED",
            AccessibilityEventLogPolicy.eventTypeName(
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            ),
        )
        assertEquals(
            "WINDOW_STATE_CHANGED",
            AccessibilityEventLogPolicy.eventTypeName(
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            ),
        )
    }

    @Test
    fun unsupportedEventTypeIsIgnored() {
        assertNull(
            AccessibilityEventLogPolicy.eventTypeName(
                AccessibilityEvent.TYPE_VIEW_CLICKED,
            ),
        )
    }

    @Test
    fun throttleSuppressesRepeatedEventTypeWithinWindow() {
        val throttle = AccessibilityEventLogThrottle(throttleWindowMillis = 300L)

        assertTrue(
            throttle.shouldLog(
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                eventTimeMillis = 1_000L,
            ),
        )
        assertFalse(
            throttle.shouldLog(
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                eventTimeMillis = 1_250L,
            ),
        )
        assertTrue(
            throttle.shouldLog(
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                eventTimeMillis = 1_300L,
            ),
        )
    }

    @Test
    fun throttleDoesNotSuppressDifferentSupportedEventType() {
        val throttle = AccessibilityEventLogThrottle(throttleWindowMillis = 300L)

        assertTrue(
            throttle.shouldLog(
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                eventTimeMillis = 1_000L,
            ),
        )
        assertTrue(
            throttle.shouldLog(
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                eventTimeMillis = 1_100L,
            ),
        )
    }

    @Test
    fun throttleIgnoresUnsupportedEventType() {
        val throttle = AccessibilityEventLogThrottle(throttleWindowMillis = 300L)

        assertFalse(
            throttle.shouldLog(
                AccessibilityEvent.TYPE_VIEW_CLICKED,
                eventTimeMillis = 1_000L,
            ),
        )
    }
}
