package com.yunki.youtubeskip.accessibility

import android.content.ComponentName
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class AccessibilityServiceStatusTest {
    @Test
    fun exactFullyQualifiedComponentMatches() {
        val componentName = ComponentName(
            TARGET_PACKAGE_NAME,
            TARGET_CLASS_NAME,
        )

        assertTrue(AccessibilityServiceStatus.isTargetComponent(componentName))
    }

    @Test
    fun flattenedComponentStringMatches() {
        assertTrue(
            AccessibilityServiceStatus.containsTargetFlattenedComponent(
                "$TARGET_PACKAGE_NAME/$TARGET_CLASS_NAME",
            ),
        )
    }

    @Test
    fun unrelatedAccessibilityServiceDoesNotMatch() {
        assertFalse(
            AccessibilityServiceStatus.containsTargetFlattenedComponent(
                "com.example.other/com.example.other.OtherAccessibilityService",
            ),
        )
    }

    @Test
    fun malformedValueDoesNotMatch() {
        assertFalse(
            AccessibilityServiceStatus.containsTargetFlattenedComponent(
                "this is not a flattened component",
            ),
        )
    }

    @Test
    fun emptyValueDoesNotMatch() {
        assertFalse(AccessibilityServiceStatus.containsTargetFlattenedComponent(""))
    }

    private companion object {
        const val TARGET_PACKAGE_NAME = "com.yunki.youtubeskip"
        const val TARGET_CLASS_NAME =
            "com.yunki.youtubeskip.accessibility.YouTubeAccessibilityService"
    }
}
