package com.android.swingmusic.library.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.library.presentation.component.LibraryMessage
import com.android.swingmusic.library.presentation.component.MosaicTileSize
import com.android.swingmusic.library.presentation.component.LibrarySnackbarHost
import com.android.swingmusic.library.presentation.component.PlaylistArtwork
import com.android.swingmusic.library.presentation.component.TrackMenu
import com.android.swingmusic.library.presentation.event.PlaylistUiEffect
import com.android.swingmusic.library.presentation.event.PlaylistUiEvent
import com.android.swingmusic.library.presentation.state.PlaylistUiState
import com.android.swingmusic.library.presentation.viewmodel.PlaylistViewModel
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.presentation.component.FilledTextField
import com.android.swingmusic.uicomponent.presentation.component.PlayAndShuffleButtons
import com.android.swingmusic.uicomponent.presentation.component.ScrimTopBar
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.util.BlurTransformation
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.android.swingmusic.uicomponent.presentation.util.formattedAlbumDuration
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

private const val PLAYLIST_EDITS_ENABLED = false

@Destination
@Composable
fun PlaylistScreen(
    playlistId: String,
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
) {
    PlaylistPage(playlistId = playlistId, navigator = navigator, mediaControllerViewModel = mediaControllerViewModel)
}

@Composable
private fun PlaylistPage(
    playlistId: String,
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
    viewModel: PlaylistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onEvent(PlaylistUiEvent.OnImagePicked(it.toString())) }
    }

    fun showMessage(message: String, withQueueAction: Boolean = false) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (withQueueAction) "View Queue" else null,
                duration = SnackbarDuration.Short
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) navigator.gotoQueueScreen()
        }
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            PlaylistUiEffect.NavigateBack -> navigator.navigateBack()
            PlaylistUiEffect.LaunchImagePicker -> imagePicker.launch("image/*")
            is PlaylistUiEffect.ShowSnackbar -> showMessage(effect.message)
            is PlaylistUiEffect.PlayTracks -> {
                val source = QueueSource.PLAYLIST(id = playlistId, name = uiState.playlist?.name.orEmpty())
                if (effect.shuffle) {
                    mediaControllerViewModel.initQueueFromGivenSource(tracks = effect.tracks, source = source)
                    mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnToggleShuffleMode())
                } else {
                    mediaControllerViewModel.onQueueEvent(
                        QueueEvent.RecreateQueue(source = source, queue = effect.tracks, clickedTrackIndex = effect.startIndex)
                    )
                }
            }
        }
    }

    PlaylistScreenContent(
        uiState = uiState,
        nowPlayingTrackHash = playerUiState.nowPlayingTrack?.trackHash,
        playbackState = playerUiState.playbackState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onGotoAlbum = { navigator.gotoAlbumWithInfo(it) },
        onGotoArtist = { navigator.gotoArtistInfo(it) },
        onGotoFolder = { name, path -> navigator.gotoSourceFolder(name, path) },
        onQueueTrack = { track, playNext ->
            val source = QueueSource.PLAYLIST(id = playlistId, name = uiState.playlist?.name.orEmpty())
            mediaControllerViewModel.onQueueEvent(
                if (playNext) QueueEvent.PlayNext(track = track, source = source)
                else QueueEvent.AddToQueue(track = track, source = source)
            )
            showMessage(if (playNext) "Track added to play next" else "Track added to queue", withQueueAction = true)
        },
        onMessage = { showMessage(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistScreenContent(
    uiState: PlaylistUiState,
    nowPlayingTrackHash: String?,
    playbackState: PlaybackState,
    onEvent: (PlaylistUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onGotoAlbum: (albumHash: String) -> Unit,
    onGotoArtist: (artistHash: String) -> Unit,
    onGotoFolder: (name: String, path: String) -> Unit,
    onQueueTrack: (track: Track, playNext: Boolean) -> Unit,
    onMessage: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    var menuTrack by remember { mutableStateOf<Pair<Track, Int>?>(null) }

    // Load the next page as the end of the list comes into view
    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 5
        }
    }
    LaunchedEffect(nearEnd, uiState.tracks.size) {
        if (nearEnd && uiState.hasMoreTracks) onEvent(PlaylistUiEvent.OnLoadMoreTracks)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { LibrarySnackbarHost(snackbarHostState) },
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            val playlist = uiState.playlist
            when {
                playlist != null -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    item(key = "header") {
                        PlaylistHeader(
                            playlist = playlist,
                            baseUrl = uiState.baseUrl,
                            onPlay = { onEvent(PlaylistUiEvent.OnPlayClicked) },
                            onShuffle = { onEvent(PlaylistUiEvent.OnShuffleClicked) }
                        )
                    }

                    if (uiState.tracks.isEmpty()) {
                        item(key = "empty") {
                            LibraryMessage(
                                title = "No tracks yet",
                                message = "Add tracks from any track, album or artist menu.",
                                actionLabel = null,
                                onAction = {}
                            )
                        }
                    }

                    itemsIndexed(uiState.tracks, key = { index, track -> "$index-${track.trackHash}" }) { index, track ->
                        TrackItem(
                            track = track,
                            showMenuIcon = true,
                            isCurrentTrack = track.trackHash == nowPlayingTrackHash,
                            playbackState = playbackState,
                            baseUrl = uiState.baseUrl,
                            onClickTrackItem = { onEvent(PlaylistUiEvent.OnTrackClicked(index)) },
                            onClickMoreVert = { menuTrack = it to index }
                        )
                    }

                    if (uiState.isLoadingMoreTracks) {
                        item(key = "loadingMore") {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }

                    item(key = "bottomSpace") { Spacer(modifier = Modifier.height(200.dp)) }
                }

                uiState.isLoadingPlaylist -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LibraryMessage(
                        title = "Couldn't load playlist",
                        message = uiState.errorLoadingPlaylist,
                        actionLabel = "Retry",
                        onAction = { onEvent(PlaylistUiEvent.OnRetry) }
                    )
                }
            }

            ScrimTopBar(
                listState = listState,
                onBack = { onEvent(PlaylistUiEvent.OnBackClicked) },
                actions = {
                    if (playlist != null) {
                        IconButton(onClick = { onEvent(PlaylistUiEvent.OnMoreClicked) }) {
                            Icon(imageVector = Icons.Rounded.MoreHoriz, contentDescription = "Playlist options")
                        }
                    }
                }
            )
        }
    }

    menuTrack?.let { (track, index) ->
        TrackMenu(
            track = track,
            isFavourite = uiState.tracks.getOrNull(index)?.isFavorite ?: track.isFavorite,
            baseUrl = uiState.baseUrl,
            onDismiss = { menuTrack = null },
            onToggleFavourite = { trackHash, isFavorite ->
                onEvent(PlaylistUiEvent.OnToggleTrackFavourite(trackHash, isFavorite))
            },
            onGotoAlbum = onGotoAlbum,
            onGotoArtist = onGotoArtist,
            onGotoFolder = onGotoFolder,
            onQueue = onQueueTrack,
            onMessage = onMessage,
            onRemoveFromPlaylist = { onEvent(PlaylistUiEvent.OnRemoveTrack(track, index)) }
        )
    }

    if (uiState.showOptionsSheet && uiState.playlist != null) {
        PlaylistOptionsSheet(playlist = uiState.playlist, baseUrl = uiState.baseUrl, onEvent = onEvent)
    }

    if (uiState.showRenameDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(PlaylistUiEvent.OnRenameDismissed) },
            title = { Text(text = "Rename Playlist") },
            text = {
                FilledTextField(
                    value = uiState.renameText,
                    onValueChange = { onEvent(PlaylistUiEvent.OnRenameTextChanged(it)) },
                    placeholder = "Playlist name",
                    errorText = uiState.renameError,
                    onDone = { onEvent(PlaylistUiEvent.OnRenameConfirmed) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(PlaylistUiEvent.OnRenameConfirmed) },
                    enabled = uiState.renameText.isNotBlank() && !uiState.isSaving
                ) { Text(text = "Save") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(PlaylistUiEvent.OnRenameDismissed) }) { Text(text = "Cancel") }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(PlaylistUiEvent.OnDeleteDismissed) },
            title = { Text(text = "Delete ${uiState.playlist?.name.orEmpty()}?") },
            text = { Text(text = "The playlist is removed from your library. Its tracks stay.") },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(PlaylistUiEvent.OnDeleteConfirmed) },
                    enabled = !uiState.isSaving
                ) { Text(text = "Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(PlaylistUiEvent.OnDeleteDismissed) }) { Text(text = "Cancel") }
            }
        )
    }
}

@Composable
private fun PlaylistHeader(
    playlist: Playlist,
    baseUrl: String,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    val backdrop = playlist.customImage?.let { "${baseUrl}img/playlist/$it" }
        ?: playlist.images.firstOrNull()?.let { "${baseUrl}img/thumbnail/$it" }

    Box(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(backdrop)
                .crossfade(true)
                .transformations(listOf(BlurTransformation(scale = 0.25f, radius = 25)))
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(surface.copy(alpha = .25F), surface.copy(alpha = .6F), surface.copy(alpha = .9F), surface)
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(106.dp))
            PlaylistArtwork(
                playlist = playlist,
                baseUrl = baseUrl,
                shape = RoundedCornerShape(8),
                modifier = Modifier.size(220.dp),
                tileSize = MosaicTileSize.MEDIUM
            )
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 2.dp)
            )
            Text(
                text = "Playlist  •  ${if (playlist.trackCount == 1) "1 Track" else "${playlist.trackCount} Tracks"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
            )
            if (playlist.duration > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = playlist.duration.formattedAlbumDuration(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            PlayAndShuffleButtons(
                onShuffle = onShuffle,
                onPlay = onPlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistOptionsSheet(
    playlist: Playlist,
    baseUrl: String,
    onEvent: (PlaylistUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        sheetState = sheetState,
        shape = RoundedCornerShape(16.dp),
        onDismissRequest = { onEvent(PlaylistUiEvent.OnOptionsDismissed) },
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .75F),
        tonalElevation = 16.dp,
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PlaylistArtwork(
                playlist = playlist,
                baseUrl = baseUrl,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (playlist.trackCount == 1) "1 track" else "${playlist.trackCount} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        androidx.compose.material3.HorizontalDivider()
        Spacer(modifier = Modifier.height(4.dp))
        // Rename, Change Image and Delete stay hidden until the server stops its playlist image
        // cleanup from deleting every playlist's custom image (it runs after updates and deletes)
        if (PLAYLIST_EDITS_ENABLED) {
            OptionRow(Icons.Rounded.Edit, "Rename") { onEvent(PlaylistUiEvent.OnRenameClicked) }
            OptionRow(Icons.Rounded.Image, "Change Image") { onEvent(PlaylistUiEvent.OnChangeImageClicked) }
        }
        if (playlist.customImage != null) {
            OptionRow(Icons.Rounded.HideImage, "Remove Image") { onEvent(PlaylistUiEvent.OnRemoveImageClicked) }
        }
        OptionRow(Icons.Rounded.PushPin, if (playlist.pinned) "Unpin" else "Pin") {
            onEvent(PlaylistUiEvent.OnTogglePinClicked)
        }
        if (PLAYLIST_EDITS_ENABLED) {
            OptionRow(Icons.Rounded.Delete, "Delete", color = MaterialTheme.colorScheme.error) {
                onEvent(PlaylistUiEvent.OnDeleteClicked)
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = color)
    }
}
