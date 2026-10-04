package com.android.swingmusic.profile.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.profile.presentation.component.ProfileSnackbarHost
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.component.SettingsGroup
import com.android.swingmusic.profile.presentation.component.SettingsGroupDivider
import com.android.swingmusic.profile.presentation.component.SettingsRow
import com.android.swingmusic.profile.presentation.event.LibraryUiEffect
import com.android.swingmusic.profile.presentation.event.LibraryUiEvent
import com.android.swingmusic.profile.presentation.state.LibraryUiState
import com.android.swingmusic.profile.presentation.viewmodel.LibraryViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Destination
@Composable
internal fun LibraryScreen(
    navigator: CommonNavigator,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            LibraryUiEffect.NavigateBack -> navigator.navigateBack()
            LibraryUiEffect.NavigateToFolders -> navigator.gotoFolders()
            is LibraryUiEffect.ShowSnackBar -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LibraryScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun LibraryScreenContent(
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { ProfileSnackbarHost(snackbarHostState) },
        topBar = { ProfileTopBar(title = "Library", onBack = { onEvent(LibraryUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.Folder,
                    title = "Folders",
                    subtitle = "Browse your music by folder",
                    onClick = { onEvent(LibraryUiEvent.OnFoldersClicked) }
                )
                SettingsGroupDivider()
                SettingsRow(
                    icon = Icons.Rounded.Download,
                    title = "Downloads",
                    subtitle = "Coming soon",
                    enabled = false,
                    showChevron = false,
                    onClick = {}
                )
            }

            if (uiState.isAdmin) {
                AdminSection(title = "Scan") {
                    SettingsGroup {
                        ScanRow(
                            title = "Quick scan",
                            subtitle = "Picks up new and changed files",
                            filled = true,
                            enabled = !uiState.isStartingScan,
                            onClick = { onEvent(LibraryUiEvent.OnQuickScanClicked) }
                        )
                        SettingsGroupDivider(inset = false)
                        ScanRow(
                            title = "Full scan",
                            subtitle = "Re-reads every file. Can take a while on big libraries",
                            filled = false,
                            enabled = !uiState.isStartingScan,
                            onClick = { onEvent(LibraryUiEvent.OnFullScanClicked) }
                        )
                    }
                }

                AdminSection(title = "Root folders") {
                    RootFolders(uiState = uiState, onRetry = { onEvent(LibraryUiEvent.OnRetryRootDirs) })
                    Text(
                        text = "Add or remove folders in the web app.",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }

    if (uiState.showFullScanDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(LibraryUiEvent.OnFullScanDismissed) },
            title = { Text("Start a full scan?") },
            text = {
                Text(
                    "The server re-reads every file in your root folders. On a big library " +
                        "this can take a while, and new music may not show until it finishes."
                )
            },
            confirmButton = {
                TextButton(onClick = { onEvent(LibraryUiEvent.OnFullScanConfirmed) }) { Text("Scan") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LibraryUiEvent.OnFullScanDismissed) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AdminChip()
        }
        content()
    }
}

@Composable
private fun ScanRow(
    title: String,
    subtitle: String,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1F)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (filled) {
            Button(onClick = onClick, enabled = enabled) { Text("Scan") }
        } else {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .3F))
            ) { Text("Scan") }
        }
    }
}

@Composable
private fun RootFolders(uiState: LibraryUiState, onRetry: () -> Unit) {
    SettingsGroup {
        when {
            uiState.isLoadingRootDirs -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }

            uiState.errorLoadingRootDirs != null -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.errorLoadingRootDirs,
                    modifier = Modifier.weight(1F),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onRetry) { Text("Retry") }
            }

            uiState.rootDirs.isEmpty() -> Text(
                text = "No root folders set",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            else -> uiState.rootDirs.forEachIndexed { index, dir ->
                if (index > 0) SettingsGroupDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Text(
                        text = if (dir == "\$home") "Home directory" else dir,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun LibraryScreenContentPreview() {
    SwingMusicTheme {
        LibraryScreenContent(uiState = LibraryUiState(), onEvent = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, heightDp = 800)
@Composable
private fun LibraryScreenAdminPreview() {
    SwingMusicTheme {
        LibraryScreenContent(
            uiState = LibraryUiState(
                isAdmin = true,
                rootDirs = listOf("/home/user/Music", "/mnt/media/Music")
            ),
            onEvent = {}
        )
    }
}
