package com.android.swingmusic.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.component.SettingsGroup
import com.android.swingmusic.profile.presentation.component.SettingsGroupDivider
import com.android.swingmusic.profile.presentation.component.SwitchRow
import com.android.swingmusic.profile.presentation.event.LyricsSettingsUiEffect
import com.android.swingmusic.profile.presentation.event.LyricsSettingsUiEvent
import com.android.swingmusic.profile.presentation.state.LyricsSettingsUiState
import com.android.swingmusic.profile.presentation.viewmodel.LyricsSettingsViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
internal fun LyricsSettingsScreen(
    navigator: CommonNavigator,
    viewModel: LyricsSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            LyricsSettingsUiEffect.NavigateBack -> navigator.navigateBack()
        }
    }

    LyricsSettingsScreenContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun LyricsSettingsScreenContent(
    uiState: LyricsSettingsUiState,
    onEvent: (LyricsSettingsUiEvent) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ProfileTopBar(title = "Lyrics", onBack = { onEvent(LyricsSettingsUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroup {
                SwitchRow(
                    title = "Word-by-word Sync",
                    checked = uiState.wordSweep,
                    onCheckedChange = { onEvent(LyricsSettingsUiEvent.OnWordSweepChange(it)) }
                )
            }

            SettingsGroup {
                SwitchRow(
                    title = "Use lyrics plugin",
                    subtitle = "Find lyrics online through the server's plugin",
                    checked = uiState.usePlugin,
                    onCheckedChange = { onEvent(LyricsSettingsUiEvent.OnUsePluginChange(it)) }
                )
                SettingsGroupDivider(inset = false)
                SwitchRow(
                    title = "Download missing lyrics",
                    subtitle = "When a track has none, fetch and save them automatically",
                    checked = uiState.autoDownload,
                    enabled = uiState.usePlugin,
                    onCheckedChange = { onEvent(LyricsSettingsUiEvent.OnAutoDownloadChange(it)) }
                )
                SettingsGroupDivider(inset = false)
                SwitchRow(
                    title = "Prefer synced lyrics",
                    subtitle = "Use online synced lyrics over local unsynced ones",
                    checked = uiState.preferSynced,
                    enabled = uiState.usePlugin,
                    onCheckedChange = { onEvent(LyricsSettingsUiEvent.OnPreferSyncedChange(it)) }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun LyricsSettingsScreenContentPreview() {
    SwingMusicTheme {
        LyricsSettingsScreenContent(
            uiState = LyricsSettingsUiState(usePlugin = true, autoDownload = true),
            onEvent = {}
        )
    }
}
