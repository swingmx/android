package com.android.swingmusic.library.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.library.presentation.component.LibraryMessage
import com.android.swingmusic.library.presentation.component.LibrarySnackbarHost
import com.android.swingmusic.library.presentation.component.SectionHeader
import com.android.swingmusic.library.presentation.component.TrackMenu
import com.android.swingmusic.library.presentation.event.FavouritesUiEffect
import com.android.swingmusic.library.presentation.event.FavouritesUiEvent
import com.android.swingmusic.library.presentation.state.FavouritesUiState
import com.android.swingmusic.library.presentation.viewmodel.FavouritesViewModel
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.presentation.component.AlbumItem
import com.android.swingmusic.uicomponent.presentation.component.ArtistItem
import com.android.swingmusic.uicomponent.presentation.component.LargeTitle
import com.android.swingmusic.uicomponent.presentation.component.LargeTitleTopBar
import com.android.swingmusic.uicomponent.presentation.component.PlayAndShuffleButtons
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.component.largeTitleTopBarHeight
import com.android.swingmusic.uicomponent.presentation.component.rememberLargeTitleState
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

private const val FAVOURITES_TITLE = "Favourites"

@Destination
@Composable
fun FavouritesScreen(
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
) {
    Favourites(navigator = navigator, mediaControllerViewModel = mediaControllerViewModel)
}

@Composable
private fun Favourites(
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
    viewModel: FavouritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(FavouritesUiEvent.OnScreenResumed)
    }

    fun showMessage(message: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(message, actionLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) onAction()
        }
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            FavouritesUiEffect.NavigateToProfile -> navigator.gotoProfile()
            FavouritesUiEffect.NavigateToTracks -> navigator.gotoFavouriteTracks()
            FavouritesUiEffect.NavigateToAlbums -> navigator.gotoFavouriteAlbums()
            FavouritesUiEffect.NavigateToArtists -> navigator.gotoFavouriteArtists()
            is FavouritesUiEffect.NavigateToAlbum -> navigator.gotoAlbumWithInfo(effect.albumHash)
            is FavouritesUiEffect.NavigateToArtist -> navigator.gotoArtistInfo(effect.artistHash)
            is FavouritesUiEffect.PlayTracks ->
                playFavourites(mediaControllerViewModel, effect.tracks, effect.startIndex, effect.shuffle)

            is FavouritesUiEffect.ShowSnackbar -> if (effect.undoable) {
                showMessage(effect.message, "Undo") { viewModel.onEvent(FavouritesUiEvent.OnUndoUnfavourite) }
            } else {
                showMessage(effect.message)
            }
        }
    }

    FavouritesScreenContent(
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
                    viewModel.onEvent(FavouritesUiEvent.OnUnfavouriteTrack(track))
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

internal fun playFavourites(
    mediaControllerViewModel: MediaControllerViewModel,
    tracks: List<Track>,
    startIndex: Int,
    shuffle: Boolean,
) {
    if (shuffle) {
        mediaControllerViewModel.initQueueFromGivenSource(tracks = tracks, source = QueueSource.FAVORITE)
        mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnToggleShuffleMode())
    } else {
        mediaControllerViewModel.onQueueEvent(
            QueueEvent.RecreateQueue(source = QueueSource.FAVORITE, queue = tracks, clickedTrackIndex = startIndex)
        )
    }
}

internal fun queueFavourite(mediaControllerViewModel: MediaControllerViewModel, track: Track, playNext: Boolean) {
    mediaControllerViewModel.onQueueEvent(
        if (playNext) QueueEvent.PlayNext(track = track, source = QueueSource.FAVORITE)
        else QueueEvent.AddToQueue(track = track, source = QueueSource.FAVORITE)
    )
}

@Composable
private fun FavouritesScreenContent(
    uiState: FavouritesUiState,
    nowPlayingTrackHash: String?,
    playbackState: PlaybackState,
    onEvent: (FavouritesUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    trackMenu: @Composable (track: Track, onDismiss: () -> Unit) -> Unit = { _, _ -> },
) {
    val listState = rememberLazyListState()
    val titleState = rememberLargeTitleState(listState)
    var menuTrack by remember { mutableStateOf<Track?>(null) }
    val favourites = uiState.favourites

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = largeTitleTopBarHeight(), bottom = 200.dp)
        ) {
            item(key = "title") {
                LargeTitle(text = FAVOURITES_TITLE, state = titleState)
            }

            when {
                favourites != null -> {
                    val isEmpty = favourites.tracks.isEmpty() && favourites.albums.isEmpty() && favourites.artists.isEmpty()
                    if (isEmpty) {
                        item(key = "empty") {
                            LibraryMessage(
                                title = "No favourites yet",
                                message = "Tap the heart on a track, album or artist and it shows up here.",
                                actionLabel = null,
                                onAction = {}
                            )
                        }
                    }

                    if (favourites.tracks.isNotEmpty()) {
                        item(key = "tracksHeader") {
                            SectionHeader(
                                title = "Tracks",
                                count = favourites.trackCount,
                                onSeeAll = { onEvent(FavouritesUiEvent.OnSeeAllTracksClicked) }
                            )
                        }
                        item(key = "playButtons") {
                            PlayAndShuffleButtons(
                                onShuffle = { onEvent(FavouritesUiEvent.OnShuffleClicked) },
                                onPlay = { onEvent(FavouritesUiEvent.OnPlayClicked) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        itemsIndexed(favourites.tracks, key = { _, track -> "track-${track.trackHash}" }) { index, track ->
                            TrackItem(
                                track = track,
                                showMenuIcon = true,
                                isCurrentTrack = track.trackHash == nowPlayingTrackHash,
                                playbackState = playbackState,
                                baseUrl = uiState.baseUrl,
                                onClickTrackItem = { onEvent(FavouritesUiEvent.OnTrackClicked(index)) },
                                onClickMoreVert = { menuTrack = it }
                            )
                        }
                    }

                    if (favourites.albums.isNotEmpty()) {
                        item(key = "albumsHeader") {
                            SectionHeader(
                                title = "Albums",
                                count = favourites.albumCount,
                                onSeeAll = { onEvent(FavouritesUiEvent.OnSeeAllAlbumsClicked) }
                            )
                        }
                        item(key = "albums") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(favourites.albums, key = { it.albumHash }) { album ->
                                    // The card only sizes its image, so the row item gets a fixed width
                                    Box(modifier = Modifier.width(156.dp)) {
                                        AlbumItem(
                                            modifier = Modifier.fillMaxWidth(),
                                            album = album,
                                            showDate = false,
                                            baseUrl = uiState.baseUrl,
                                            onClick = { onEvent(FavouritesUiEvent.OnAlbumClicked(it)) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (favourites.artists.isNotEmpty()) {
                        item(key = "artistsHeader") {
                            SectionHeader(
                                title = "Artists",
                                count = favourites.artistCount,
                                onSeeAll = { onEvent(FavouritesUiEvent.OnSeeAllArtistsClicked) }
                            )
                        }
                        item(key = "artists") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(favourites.artists, key = { it.artistHash }) { artist ->
                                    // The card only sizes its image, so the row item gets a fixed width
                                    Box(modifier = Modifier.width(136.dp)) {
                                        ArtistItem(
                                            modifier = Modifier.fillMaxWidth(),
                                            artist = artist,
                                            baseUrl = uiState.baseUrl,
                                            onClick = { onEvent(FavouritesUiEvent.OnArtistClicked(it)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                uiState.isLoadingFavourites -> item(key = "loading") {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    }
                }

                else -> item(key = "error") {
                    LibraryMessage(
                        title = "Couldn't load favourites",
                        message = uiState.errorLoadingFavourites,
                        actionLabel = "Retry",
                        onAction = { onEvent(FavouritesUiEvent.OnRetry) }
                    )
                }
            }
        }

        LargeTitleTopBar(
            title = FAVOURITES_TITLE,
            state = titleState,
            avatarPath = uiState.avatarPath,
            onProfileClick = { onEvent(FavouritesUiEvent.OnProfileClicked) }
        )

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            LibrarySnackbarHost(snackbarHostState)
        }
    }

    menuTrack?.let { track -> trackMenu(track) { menuTrack = null } }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun FavouritesScreenContentPreview() {
    SwingMusicTheme {
        FavouritesScreenContent(
            uiState = FavouritesUiState(isLoadingFavourites = false),
            nowPlayingTrackHash = null,
            playbackState = PlaybackState.PAUSED,
            onEvent = {}
        )
    }
}
