package com.yunki.youtubeskip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yunki.youtubeskip.R
import com.yunki.youtubeskip.accessibility.AccessibilityServiceStatus
import com.yunki.youtubeskip.settings.AppPreferences
import com.yunki.youtubeskip.settings.LastClickResult
import com.yunki.youtubeskip.settings.SkipStatistics
import java.text.DateFormat
import java.util.Date

@Composable
fun YouTubeSkipApp(onOpenAccessibilitySettings: () -> Unit = {}) {
    val isAccessibilityServiceEnabled = rememberAccessibilityServiceEnabled()
    val appSettingsState = rememberAppSettingsState()

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            HomeScreen(
                isAccessibilityServiceEnabled = isAccessibilityServiceEnabled,
                isAutomaticSkipEnabled = appSettingsState.isAutomaticSkipEnabled,
                skipStatistics = appSettingsState.skipStatistics,
                onAutomaticSkipEnabledChange = appSettingsState.onAutomaticSkipEnabledChange,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            )
        }
    }
}

@Composable
fun HomeScreen(
    isAccessibilityServiceEnabled: Boolean,
    isAutomaticSkipEnabled: Boolean,
    skipStatistics: SkipStatistics,
    onAutomaticSkipEnabledChange: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.event_logging_logcat_note),
                style = MaterialTheme.typography.bodyMedium,
            )
            StatusRow(
                label = stringResource(R.string.accessibility_status_label),
                value = stringResource(
                    if (isAccessibilityServiceEnabled) R.string.status_enabled else R.string.status_disabled,
                ),
            )
            AutomaticSkipRow(
                isAutomaticSkipEnabled = isAutomaticSkipEnabled,
                onAutomaticSkipEnabledChange = onAutomaticSkipEnabledChange,
            )
            StatusRow(
                label = stringResource(R.string.target_app_label),
                value = stringResource(R.string.target_app_youtube_value),
            )
            StatusRow(
                label = stringResource(R.string.successful_skips_label),
                value = skipStatistics.successfulSkipCount.toString(),
            )
            StatusRow(
                label = stringResource(R.string.last_successful_skip_label),
                value = formatLastSuccessfulSkip(skipStatistics.lastSuccessfulSkipTimestampMillis),
            )
            StatusRow(
                label = stringResource(R.string.last_click_result_label),
                value = skipStatistics.lastClickResult.displayText,
            )
            Button(
                onClick = onOpenAccessibilitySettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.open_accessibility_settings))
            }
        }
    }
}

@Composable
private fun AutomaticSkipRow(
    isAutomaticSkipEnabled: Boolean,
    onAutomaticSkipEnabledChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.automatic_skip_label),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.automatic_skip_supporting_text),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Switch(
            checked = isAutomaticSkipEnabled,
            onCheckedChange = onAutomaticSkipEnabledChange,
        )
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun rememberAccessibilityServiceEnabled(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isEnabled by remember {
        mutableStateOf(AccessibilityServiceStatus.isYouTubeSkipServiceEnabled(context))
    }

    DisposableEffect(context, lifecycleOwner) {
        fun refreshEnabledState() {
            isEnabled = AccessibilityServiceStatus.isYouTubeSkipServiceEnabled(context)
        }

        refreshEnabledState()

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshEnabledState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return isEnabled
}

private data class AppSettingsState(
    val isAutomaticSkipEnabled: Boolean,
    val skipStatistics: SkipStatistics,
    val onAutomaticSkipEnabledChange: (Boolean) -> Unit,
)

@Composable
private fun rememberAppSettingsState(): AppSettingsState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferences = remember(context) { AppPreferences(context) }
    var isAutomaticSkipEnabled by remember {
        mutableStateOf(preferences.automaticSkipEnabled)
    }
    var skipStatistics by remember {
        mutableStateOf(preferences.skipStatistics())
    }

    DisposableEffect(context, lifecycleOwner) {
        fun refreshSettingsState() {
            isAutomaticSkipEnabled = preferences.automaticSkipEnabled
            skipStatistics = preferences.skipStatistics()
        }

        refreshSettingsState()

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshSettingsState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return AppSettingsState(
        isAutomaticSkipEnabled = isAutomaticSkipEnabled,
        skipStatistics = skipStatistics,
        onAutomaticSkipEnabledChange = { enabled ->
            preferences.setAutomaticSkipEnabled(enabled)
            isAutomaticSkipEnabled = enabled
        },
    )
}

private fun formatLastSuccessfulSkip(timestampMillis: Long?): String {
    return timestampMillis
        ?.let { DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it)) }
        ?: LastClickResult.NONE.displayText
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            isAccessibilityServiceEnabled = false,
            isAutomaticSkipEnabled = true,
            skipStatistics = SkipStatistics(
                successfulSkipCount = 0,
                lastSuccessfulSkipTimestampMillis = null,
                lastClickResult = LastClickResult.NONE,
            ),
            onAutomaticSkipEnabledChange = {},
            onOpenAccessibilitySettings = {},
        )
    }
}
