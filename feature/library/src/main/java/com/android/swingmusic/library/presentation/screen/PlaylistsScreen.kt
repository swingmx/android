package com.android.swingmusic.library.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.library.presentation.component.LibraryMessage
import com.android.swingmusic.library.presentation.component.MosaicTileSize
import com.android.swingmusic.library.presentation.component.LibrarySnackbarHost
import com.android.swingmusic.library.presentation.component.LibraryTopBar
import com.android.swingmusic.library.presentation.component.PlaylistArtwork
import com.android.swingmusic.library.presentation.event.PlaylistsUiEffect
import com.android.swingmusic.library.presentation.event.PlaylistsUiEvent
import com.android.swingmusic.library.presentation.state.PlaylistsUiState
import com.android.swingmusic.library.presentation.viewmodel.PlaylistsViewModel
import com.android.swingmusic.uicomponent.presentation.component.FilledTextField
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Destination
@Composable
fun PlaylistsScreen(
    navigator: CommonNavigator,
) {
    Playlists(navigator = navigator)
}

@Composable
private fun Playlists(
    navigator: CommonNavigator,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(PlaylistsUiEvent.OnScreenResumed)
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            PlaylistsUiEffect.NavigateBack -> navigator.navigateBack()
            is PlaylistsUiEffect.NavigateToPlaylist -> navigator.gotoPlaylist(effect.playlistId)
            is PlaylistsUiEffect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
        }
    }

    PlaylistsScreenContent(uiState = uiState, onEvent = viewModel::onEvent, snackbarHostState = snackbarHostState)
}

@Composable
private fun PlaylistsScreenContent(
    uiState: PlaylistsUiState,
    onEvent: (PlaylistsUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { LibrarySnackbarHost(snackbarHostState) },
        topBar = { LibraryTopBar(title = "Playlists", onBack = { onEvent(PlaylistsUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 200.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "new", span = { GridItemSpan(maxLineSpan) }) {
                NewPlaylistRow(onClick = { onEvent(PlaylistsUiEvent.OnNewPlaylistClicked) })
            }

            when {
                uiState.isLoadingPlaylists -> item(key = "loading", span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    }
                }

                uiState.errorLoadingPlaylists != null -> item(key = "error", span = { GridItemSpan(maxLineSpan) }) {
                    LibraryMessage(
                        title = "Couldn't load playlists",
                        message = uiState.errorLoadingPlaylists,
                        actionLabel = "Retry",
                        onAction = { onEvent(PlaylistsUiEvent.OnRetry) }
                    )
                }

                uiState.pinnedPlaylists.isEmpty() && uiState.otherPlaylists.isEmpty() ->
                    item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                        LibraryMessage(
                            title = "No playlists yet",
                            message = "Create one here, or add an album, artist or track to a new playlist from its menu.",
                            actionLabel = null,
                            onAction = {}
                        )
                    }

                else -> {
                    val showGroups = uiState.pinnedPlaylists.isNotEmpty()
                    if (showGroups) {
                        sectionTitle("Pinned")
                        playlistCards(uiState.pinnedPlaylists, uiState.baseUrl, onEvent)
                        sectionTitle("Other playlists")
                    }
                    playlistCards(uiState.otherPlaylists, uiState.baseUrl, onEvent)
                }
            }
        }
    }

    if (uiState.showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(PlaylistsUiEvent.OnNewPlaylistDismissed) },
            title = { Text(text = "New Playlist") },
            text = {
                FilledTextField(
                    value = uiState.newPlaylistName,
                    onValueChange = { onEvent(PlaylistsUiEvent.OnNewPlaylistNameChanged(it)) },
                    placeholder = "Playlist name",
                    errorText = uiState.newPlaylistError,
                    onDone = { onEvent(PlaylistsUiEvent.OnCreatePlaylist) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(PlaylistsUiEvent.OnCreatePlaylist) },
                    enabled = uiState.newPlaylistName.isNotBlank() && !uiState.isCreatingPlaylist
                ) { Text(text = "Create") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(PlaylistsUiEvent.OnNewPlaylistDismissed) }) { Text(text = "Cancel") }
            }
        )
    }
}

private fun LazyGridScope.sectionTitle(title: String) {
    item(key = "title-$title", span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
    }
}

private fun LazyGridScope.playlistCards(
    playlists: List<Playlist>,
    baseUrl: String,
    onEvent: (PlaylistsUiEvent) -> Unit,
) {
    items(playlists, key = { it.id }) { playlist ->
        PlaylistCard(
            playlist = playlist,
            baseUrl = baseUrl,
            onClick = { onEvent(PlaylistsUiEvent.OnPlaylistClicked(playlist.id)) }
        )
    }
}

@Composable
private fun NewPlaylistRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = "New Playlist", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, baseUrl: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            // Keeps the text clear of the rounded bottom corners
            .padding(bottom = 8.dp)
    ) {
        PlaylistArtwork(
            playlist = playlist,
            baseUrl = baseUrl,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            tileSize = MosaicTileSize.MEDIUM
        )
        Spacer(modifier = Modifier.size(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (playlist.pinned) {
                Icon(
                    imageVector = Icons.Rounded.PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = if (playlist.trackCount == 1) "1 track" else "${playlist.trackCount} tracks",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PlaylistsScreenContentPreview() {
    SwingMusicTheme {
        PlaylistsScreenContent(
            uiState = PlaylistsUiState(
                isLoadingPlaylists = false,
                pinnedPlaylists = listOf(
                    Playlist(id = "1", name = "Road Trip", customImage = null, images = emptyList(), trackCount = 24, pinned = true)
                ),
                otherPlaylists = listOf(
                    Playlist(id = "2", name = "Focus", customImage = null, images = emptyList(), trackCount = 74),
                    Playlist(id = "3", name = "Late Night", customImage = null, images = emptyList(), trackCount = 1)
                )
            ),
            onEvent = {}
        )
    }
}
