package com.android.swingmusic.library.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.library.presentation.component.LibraryMessage
import com.android.swingmusic.library.presentation.component.LibrarySnackbarHost
import com.android.swingmusic.library.presentation.component.LibraryTopBar
import com.android.swingmusic.library.presentation.component.TrackMenu
import com.android.swingmusic.library.presentation.event.FavouriteTracksUiEffect
import com.android.swingmusic.library.presentation.event.FavouriteTracksUiEvent
import com.android.swingmusic.library.presentation.state.FavouriteTracksUiState
import com.android.swingmusic.library.presentation.viewmodel.FavouriteTracksViewModel
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.presentation.component.PlayAndShuffleButtons
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Destination
@Composable
fun FavouriteTracksScreen(
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
) {
    FavouriteTracks(navigator = navigator, mediaControllerViewModel = mediaControllerViewModel)
}

@Composable
private fun FavouriteTracks(
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
    viewModel: FavouriteTracksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMessage(message: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(message, actionLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) onAction()
        }
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            FavouriteTracksUiEffect.NavigateBack -> navigator.navigateBack()
            is FavouriteTracksUiEffect.PlayTracks ->
                playFavourites(mediaControllerViewModel, effect.tracks, effect.startIndex, effect.shuffle)

            is FavouriteTracksUiEffect.ShowSnackbar -> if (effect.undoable) {
                showMessage(effect.message, "Undo") { viewModel.onEvent(FavouriteTracksUiEvent.OnUndoUnfavourite) }
            } else {
                showMessage(effect.message)
            }
        }
    }

    FavouriteTracksScreenContent(
        uiState = uiState,
        nowPlayingTrackHash = playerUiState.nowPlayingTrack?.trackHash,
        playbackState = playerUiState.playbackState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        trackMenu = { track, onDismiss ->
            TrackMenu(
                track = track,
                isFavourite = true,
                baseUrl = uiState.baseUrl,
                onDismiss = onDismiss,
                onToggleFavourite = { _, _ ->
                    onDismiss()
                    viewModel.onEvent(FavouriteTracksUiEvent.OnUnfavouriteTrack(track))
                },
                onGotoAlbum = { navigator.gotoAlbumWithInfo(it) },
                onGotoArtist = { navigator.gotoArtistInfo(it) },
                onGotoFolder = { name, path -> navigator.gotoSourceFolder(name, path) },
                onQueue = { queued, playNext ->
                    queueFavourite(mediaControllerViewModel, queued, playNext)
                    showMessage(
                        if (playNext) "Track added to play next" else "Track added to queue",
                        "View Queue"
                    ) { navigator.gotoQueueScreen() }
                },
                onMessage = { showMessage(it) }
            )
        }
    )
}

@Composable
private fun FavouriteTracksScreenContent(
    uiState: FavouriteTracksUiState,
    nowPlayingTrackHash: String?,
    playbackState: PlaybackState,
    onEvent: (FavouriteTracksUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    trackMenu: @Composable (track: Track, onDismiss: () -> Unit) -> Unit,
) {
    val listState = rememberLazyListState()
    var menuTrack by remember { mutableStateOf<Track?>(null) }

    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 5
        }
    }
    LaunchedEffect(nearEnd, uiState.tracks.size) {
        if (nearEnd && !uiState.endReached) onEvent(FavouriteTracksUiEvent.OnLoadMoreTracks)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { LibrarySnackbarHost(snackbarHostState) },
        topBar = {
            LibraryTopBar(title = "Favourite Tracks", onBack = { onEvent(FavouriteTracksUiEvent.OnBackClicked) })
        }
    ) { paddingValues ->
        when {
            uiState.isLoadingTracks -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.errorLoadingTracks != null -> LibraryMessage(
                title = "Couldn't load favourite tracks",
                message = uiState.errorLoadingTracks,
                actionLabel = "Retry",
                onAction = { onEvent(FavouriteTracksUiEvent.OnRetry) },
                modifier = Modifier.padding(paddingValues)
            )

            uiState.tracks.isEmpty() -> LibraryMessage(
                title = "No favourite tracks",
                message = "Tap the heart on a track and it shows up here.",
                actionLabel = null,
                onAction = {},
                modifier = Modifier.padding(paddingValues)
            )

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(bottom = 200.dp)
            ) {
                item(key = "header") {
                    uiState.total?.let {
                        Text(
                            text = if (it == 1) "1 track" else "$it tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                    PlayAndShuffleButtons(
                        onShuffle = { onEvent(FavouriteTracksUiEvent.OnShuffleClicked) },
                        onPlay = { onEvent(FavouriteTracksUiEvent.OnPlayClicked) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                itemsIndexed(uiState.tracks, key = { _, track -> track.trackHash }) { index, track ->
                    TrackItem(
                        track = track,
                        showMenuIcon = true,
                        isCurrentTrack = track.trackHash == nowPlayingTrackHash,
                        playbackState = playbackState,
                        baseUrl = uiState.baseUrl,
                        onClickTrackItem = { onEvent(FavouriteTracksUiEvent.OnTrackClicked(index)) },
                        onClickMoreVert = { menuTrack = it }
                    )
                }
                if (uiState.isLoadingMoreTracks) {
                    item(key = "loadingMore") {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }

    menuTrack?.let { track -> trackMenu(track) { menuTrack = null } }
}
