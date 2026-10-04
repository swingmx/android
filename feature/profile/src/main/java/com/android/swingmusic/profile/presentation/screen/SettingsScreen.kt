package com.android.swingmusic.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.component.SettingsGroup
import com.android.swingmusic.profile.presentation.component.SettingsGroupDivider
import com.android.swingmusic.profile.presentation.component.SettingsRow
import com.android.swingmusic.profile.presentation.event.SettingsUiEffect
import com.android.swingmusic.profile.presentation.event.SettingsUiEvent
import com.android.swingmusic.profile.presentation.state.SettingsUiState
import com.android.swingmusic.profile.presentation.util.formatBytes
import com.android.swingmusic.profile.presentation.viewmodel.SettingsViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
internal fun SettingsScreen(
    navigator: CommonNavigator,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(SettingsUiEvent.OnScreenResumed)
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            SettingsUiEffect.NavigateBack -> navigator.navigateBack()
            SettingsUiEffect.NavigateToAccount -> navigator.gotoAccountSettings()
            SettingsUiEffect.NavigateToLyrics -> navigator.gotoLyricsSettings()
            SettingsUiEffect.NavigateToStorage -> navigator.gotoStorageSettings()
            SettingsUiEffect.NavigateToAbout -> navigator.gotoAbout()
        }
    }

    SettingsScreenContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ProfileTopBar(title = "Settings", onBack = { onEvent(SettingsUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Person,
                    title = "Account",
                    subtitle = "Username, password",
                    onClick = { onEvent(SettingsUiEvent.OnAccountClicked) }
                )
                SettingsGroupDivider()
                SettingsRow(
                    icon = Icons.Rounded.Lyrics,
                    title = "Lyrics",
                    subtitle = "Plugin, auto-download, synced",
                    onClick = { onEvent(SettingsUiEvent.OnLyricsClicked) }
                )
                SettingsGroupDivider()
                SettingsRow(
                    icon = Icons.Rounded.Storage,
                    title = "Storage",
                    subtitle = uiState.imageCacheBytes?.let { "Image cache · ${formatBytes(it)}" }
                        ?: "Image cache",
                    onClick = { onEvent(SettingsUiEvent.OnStorageClicked) }
                )
            }

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Info,
                    title = "About",
                    subtitle = "Version ${uiState.appVersion} · report a problem",
                    onClick = { onEvent(SettingsUiEvent.OnAboutClicked) }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SettingsScreenContentPreview() {
    SwingMusicTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(appVersion = "1.0.0", imageCacheBytes = 48L * 1024 * 1024),
            onEvent = {}
        )
    }
}
