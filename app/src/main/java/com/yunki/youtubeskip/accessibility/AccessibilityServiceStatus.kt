package com.yunki.youtubeskip.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.view.accessibility.AccessibilityManager

object AccessibilityServiceStatus {
    private const val SERVICE_PACKAGE_NAME = "com.yunki.youtubeskip"
    private const val SERVICE_CLASS_NAME =
        "com.yunki.youtubeskip.accessibility.YouTubeAccessibilityService"

    private val targetComponent = ComponentName(SERVICE_PACKAGE_NAME, SERVICE_CLASS_NAME)

    fun isYouTubeSkipServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
            ?: return false

        return accessibilityManager
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { serviceInfo ->
                val resolvedServiceInfo = serviceInfo.resolveInfo?.serviceInfo ?: return@any false
                componentNameOrNull(
                    packageName = resolvedServiceInfo.packageName,
                    className = resolvedServiceInfo.name,
                )?.let(::isTargetComponent) == true
            }
    }

    internal fun isTargetComponent(componentName: ComponentName?): Boolean {
        return componentName == targetComponent
    }

    internal fun isTargetFlattenedComponent(flattenedComponent: String?): Boolean {
        return flattenedComponent
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(ComponentName::unflattenFromString)
            ?.let(::isTargetComponent) == true
    }

    internal fun containsTargetFlattenedComponent(enabledServices: String?): Boolean {
        return enabledServices
            ?.split(':')
            ?.any(::isTargetFlattenedComponent) == true
    }

    private fun componentNameOrNull(
        packageName: String?,
        className: String?,
    ): ComponentName? {
        if (packageName.isNullOrBlank() || className.isNullOrBlank()) {
            return null
        }

        return ComponentName(packageName, className)
    }
}
