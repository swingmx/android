package com.android.swingmusic.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.component.SettingsGroup
import com.android.swingmusic.profile.presentation.component.SettingsGroupDivider
import com.android.swingmusic.profile.presentation.component.SettingsRow
import com.android.swingmusic.profile.presentation.component.ValueRow
import com.android.swingmusic.profile.presentation.event.AboutUiEffect
import com.android.swingmusic.profile.presentation.event.AboutUiEvent
import com.android.swingmusic.profile.presentation.state.AboutUiState
import com.android.swingmusic.profile.presentation.viewmodel.AboutViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
internal fun AboutScreen(
    navigator: CommonNavigator,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            AboutUiEffect.NavigateBack -> navigator.navigateBack()
            is AboutUiEffect.OpenUrl -> runCatching { uriHandler.openUri(effect.url) }
        }
    }

    AboutScreenContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun AboutScreenContent(
    uiState: AboutUiState,
    onEvent: (AboutUiEvent) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ProfileTopBar(title = "About", onBack = { onEvent(AboutUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroup {
                ValueRow(title = "App version", value = uiState.appVersion)
                SettingsGroupDivider(inset = false)
                ValueRow(
                    title = "Server version",
                    value = uiState.serverVersion?.let { if (it.startsWith("v")) it else "v$it" }
                        ?: "Unavailable"
                )
            }

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.BugReport,
                    title = "Report a problem",
                    subtitle = "Opens a GitHub issue with both versions filled in",
                    onClick = { onEvent(AboutUiEvent.OnReportProblemClicked) }
                )
                SettingsGroupDivider()
                SettingsRow(
                    icon = Icons.Rounded.Code,
                    title = "Source code on GitHub",
                    onClick = { onEvent(AboutUiEvent.OnSourceCodeClicked) }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun AboutScreenContentPreview() {
    SwingMusicTheme {
        AboutScreenContent(
            uiState = AboutUiState(appVersion = "1.0.0", serverVersion = "2.1.14"),
            onEvent = {}
        )
    }
}
