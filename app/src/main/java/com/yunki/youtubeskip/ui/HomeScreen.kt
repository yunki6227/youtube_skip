package com.yunki.youtubeskip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun YouTubeSkipApp(onOpenAccessibilitySettings: () -> Unit = {}) {
    val isAccessibilityServiceEnabled = rememberAccessibilityServiceEnabled()

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            HomeScreen(
                isAccessibilityServiceEnabled = isAccessibilityServiceEnabled,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            )
        }
    }
}

@Composable
fun HomeScreen(
    isAccessibilityServiceEnabled: Boolean,
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
                text = stringResource(
                    if (isAccessibilityServiceEnabled) {
                        R.string.accessibility_service_enabled
                    } else {
                        R.string.accessibility_service_disabled
                    },
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.target_app_youtube),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.event_logging_logcat_note),
                style = MaterialTheme.typography.bodyMedium,
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

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            isAccessibilityServiceEnabled = false,
            onOpenAccessibilitySettings = {},
        )
    }
}
