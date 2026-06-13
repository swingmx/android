package com.android.swingmusic.settings.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.settings.presentation.event.SettingsUiEffect
import com.android.swingmusic.settings.presentation.event.SettingsUiEvent
import com.android.swingmusic.settings.presentation.state.SettingsUiState
import com.android.swingmusic.settings.presentation.viewmodel.SettingsViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Destination
@Composable
internal fun SettingsScreen(
    navigator: CommonNavigator,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            SettingsUiEffect.NavigateBack -> navigator.navigateBack()
            SettingsUiEffect.NavigateToQrScan -> navigator.gotoLoginWithQrCode()
            is SettingsUiEffect.ShowSnackBar -> {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost,
        topBar = {
            SettingsTopBar(
                onBack = { onEvent(SettingsUiEvent.OnBackPressed) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileSection(
                uiState = uiState,
                onClickRePair = { onEvent(SettingsUiEvent.OnClickRePairDevice) }
            )

            HorizontalDivider()

            LyricsSection(
                uiState = uiState,
                onTogglePlugin = { onEvent(SettingsUiEvent.OnToggleUseLyricsPlugin(it)) },
                onToggleAuto = { onEvent(SettingsUiEvent.OnToggleLyricsAutoDownload(it)) },
                onToggleOverride = { onEvent(SettingsUiEvent.OnToggleLyricsOverrideUnsynced(it)) }
            )

            HorizontalDivider()

            AboutSection(uiState = uiState)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileSection(
    uiState: SettingsUiState,
    onClickRePair: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Account")

        if (uiState.serverUrl.isNotBlank()) {
            Text(
                text = "Server",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = uiState.serverUrl,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "No server connected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onClickRePair,
            enabled = !uiState.isSigningOut
        ) {
            Text(
                text = if (uiState.isSigningOut) "Signing out..." else "Re-pair device"
            )
        }
    }
}

@Composable
private fun LyricsSection(
    uiState: SettingsUiState,
    onTogglePlugin: (Boolean) -> Unit,
    onToggleAuto: (Boolean) -> Unit,
    onToggleOverride: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Lyrics")

        SettingToggleRow(
            label = "Online lyrics search",
            description = "Let the app fetch lyrics from online sources when they aren't on your server",
            checked = uiState.useLyricsPlugin,
            onCheckedChange = onTogglePlugin
        )

        SettingToggleRow(
            label = "Auto-search when missing",
            description = "Search online automatically when a track has no lyrics. Otherwise the lyrics screen shows a Search online button you can tap",
            checked = uiState.lyricsAutoDownload,
            onCheckedChange = onToggleAuto,
            enabled = uiState.useLyricsPlugin
        )

        SettingToggleRow(
            label = "Upgrade unsynced lyrics",
            description = "If a track only has plain text lyrics, look online for a version with timestamps that highlight each line as the song plays",
            checked = uiState.lyricsOverrideUnsynced,
            onCheckedChange = onToggleOverride,
            enabled = uiState.useLyricsPlugin
        )
    }
}

@Composable
private fun AboutSection(uiState: SettingsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionLabel(text = "About")
        Text(
            text = if (uiState.appVersion.isNotBlank()) {
                "Version ${uiState.appVersion}"
            } else {
                "Swing Music"
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (enabled) 0.7f else 0.3f
                )
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Preview
@Composable
private fun SettingsScreenContentPreview() {
    SwingMusicTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(
                serverUrl = "https://swingmx.local/",
                appVersion = "0.42.0",
                useLyricsPlugin = true,
                lyricsAutoDownload = true,
                lyricsOverrideUnsynced = false
            ),
            onEvent = {},
            snackbarHost = {}
        )
    }
}
