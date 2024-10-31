package com.android.swingmusic.folder.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.swingmusic.album.presentation.event.AlbumWithInfoUiEvent
import com.android.swingmusic.album.presentation.navigator.AlbumNavigator
import com.android.swingmusic.album.presentation.viewmodel.AlbumWithInfoViewModel
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.state.FoldersAndTracksState
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.BottomSheetItem
import com.android.swingmusic.uicomponent.presentation.component.FolderItem
import com.android.swingmusic.uicomponent.presentation.component.PathIndicatorItem
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.ramcosta.composedestinations.annotation.Destination
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FoldersAndTracks(
    currentFolder: Folder,
    homeDir: Folder,
    foldersAndTracksState: FoldersAndTracksState,
    currentTrackHash: String,
    playbackState: PlaybackState,
    navPaths: List<Folder>,
    onClickNavPath: (Folder) -> Unit,
    onRetry: (FolderUiEvent) -> Unit,
    onClickFolder: (Folder) -> Unit,
    onClickTrackItem: (index: Int, queue: List<Track>) -> Unit,
    onGoToTrackAlbum: (albumHash: String) -> Unit,
    baseUrl: String
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var clickedTrack: Track? by remember { mutableStateOf(null) }

    // use a double scaffold to take advantage of padding values since the app uses full screen mode
    Scaffold { it ->
        Scaffold(
            modifier = Modifier.padding(it),
            topBar = {
                Text(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 16.dp,
                        bottom = 8.dp
                    ),
                    text = "Folders",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val lazyColumnState = rememberLazyListState()
                val pathsLazyRowState = rememberLazyListState()

                LaunchedEffect(key1 = currentFolder) {
                    val index = navPaths.indexOf(currentFolder)
                    pathsLazyRowState.animateScrollToItem(if (index >= 0) index else 0)
                }
                if (showBottomSheet) {
                    ModalBottomSheet(
                        sheetState = sheetState,
                        shape = RoundedCornerShape(16.dp),
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        dragHandle = null,
                        scrimColor = Color.Black.copy(alpha = .75F),
                        tonalElevation = 16.dp,
                    ) {
                        clickedTrack?.let { track ->
                            Column {
                                Box(modifier = Modifier.offset(x = (-8).dp)) {
                                    TrackItem(
                                        track = track,
                                        onClickTrackItem = {},
                                        onClickMoreVert = {},
                                        baseUrl = baseUrl
                                    )
                                }

                                HorizontalDivider()
                            }
                        }

                        BottomSheetItem(
                            label = "Go to Album",
                            scope = scope,
                            sheetState = sheetState,
                            iconPainter = painterResource(id = R.drawable.ic_album),
                            clickedTrack = clickedTrack,
                            onClick = { track ->
                                onGoToTrackAlbum(track.albumHash)
                            },
                            onHideBottomSheet = { hide ->
                                showBottomSheet = !hide
                            }
                        )

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyColumnState
                ) {
                    // Navigation Paths
                    stickyHeader {
                        LazyRow(
                            state = pathsLazyRowState,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .fillMaxWidth()
                                .padding(start = 9.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Always display a root dir item
                            item {
                                PathIndicatorItem(
                                    folder = homeDir,
                                    isRootPath = true,
                                    isCurrentPath = homeDir.path == currentFolder.path,
                                    onClick = { navFolder ->
                                        onClickNavPath(navFolder)
                                    }
                                )
                            }

                            itemsIndexed(navPaths) { index, folder ->
                                // Ignore home dir because it is already displayed
                                if (folder.path != "\$home") {
                                    PathIndicatorItem(
                                        folder = folder,
                                        isCurrentPath = folder.path == currentFolder.path,
                                        onClick = { navFolder ->
                                            onClickNavPath(navFolder)
                                        }
                                    )
                                }
                                if (navPaths.size != 1 &&
                                    index != navPaths.lastIndex
                                ) {
                                    val tint = if (navPaths[index + 1].path == currentFolder.path)
                                        MaterialTheme.colorScheme.onSurface else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .30F)
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        tint = tint,
                                        contentDescription = "Arrow Right"
                                    )
                                }
                            }
                        }
                    }

                    if (!foldersAndTracksState.isError && !foldersAndTracksState.isLoading) {
                        // Folders
                        itemsIndexed(
                            items = foldersAndTracksState.foldersAndTracks.folders,
                            key = { _: Int, item: Folder -> item.path }
                        ) { index, folder ->
                            FolderItem(
                                folder = folder,
                                onClickFolderItem = { clickedFolder ->
                                    onClickFolder(clickedFolder)
                                },
                                onClickMoreVert = {

                                }
                            )

                            if (index == foldersAndTracksState.foldersAndTracks.folders.lastIndex &&
                                foldersAndTracksState.foldersAndTracks.tracks.isEmpty()
                            ) {
                                Spacer(modifier = Modifier.height(150.dp))
                            }
                        }

                        // Tracks
                        itemsIndexed(
                            items = foldersAndTracksState.foldersAndTracks.tracks,
                            key = { _: Int, item: Track -> item.filepath }
                        ) { index, track ->
                            TrackItem(
                                track = track,
                                showMenuIcon = true, // TODO: set true after fixing AlbumInfo bug
                                isCurrentTrack = track.trackHash == currentTrackHash,
                                playbackState = playbackState,
                                baseUrl = baseUrl,
                                onClickTrackItem = {
                                    onClickTrackItem(
                                        index, foldersAndTracksState.foldersAndTracks.tracks
                                    )
                                },
                                onClickMoreVert = {
                                    clickedTrack = it
                                    showBottomSheet = true
                                }
                            )

                            if (index == foldersAndTracksState.foldersAndTracks.tracks.lastIndex) {
                                Spacer(modifier = Modifier.height(150.dp))
                            }
                        }
                    }

                    item {
                        if (foldersAndTracksState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(strokeCap = StrokeCap.Round)
                            }
                        }
                    }

                    item {
                        if (foldersAndTracksState.isError) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxHeight()
                                    .padding(bottom = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val capsFirst =
                                        foldersAndTracksState.errorMessage.replaceFirstChar {
                                            it.titlecase(Locale.ROOT)
                                        }

                                    Text(
                                        text = capsFirst,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(onClick = {
                                        val event = FolderUiEvent.OnClickFolder(currentFolder)
                                        onRetry(event)
                                    }) {
                                        Text("RETRY")
                                    }
                                }
                            }
                        }
                    }

                    if (
                        foldersAndTracksState.foldersAndTracks.folders.isEmpty() &&
                        foldersAndTracksState.foldersAndTracks.tracks.isEmpty() &&
                        !foldersAndTracksState.isError &&
                        !foldersAndTracksState.isLoading
                    ) {
                        // This could mean the root directory is not configured or current directory is empty
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillParentMaxSize()
                                    .padding(bottom = 48.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "This directory is empty!",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                if (currentFolder.path == "\$home") {
                                    Button(onClick = {
                                        // TODO:Add Configure Dir Event
                                    }) {
                                        Text("Configure Root Directory")
                                    }
                                } else {
                                    OutlinedButton(onClick = {
                                        val event = FolderUiEvent.OnClickFolder(currentFolder)
                                        onRetry(event)
                                    }) {
                                        Text("RETRY")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * This Composable it tied to FoldersViewModel.
 *  It basically calls a stateless composable
 *  and provides hoisted states
 */
@Destination
@Composable
fun FoldersAndTracksScreen(
    foldersViewModel: FoldersViewModel = hiltViewModel(),
    albumWithInfoViewModel: AlbumWithInfoViewModel = hiltViewModel(),
    mediaControllerViewModel: MediaControllerViewModel,
    albumNavigator: AlbumNavigator
) {
    val currentFolder by remember { foldersViewModel.currentFolder }
    val foldersAndTracksState by remember { foldersViewModel.foldersAndTracks }
    val navPaths by remember { foldersViewModel.navPaths }
    val homeDir = remember { foldersViewModel.homeDir }
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    SwingMusicTheme(navBarColor = MaterialTheme.colorScheme.inverseOnSurface) {
        FoldersAndTracks(
            currentFolder = currentFolder,
            currentTrackHash = playerUiState.nowPlayingTrack?.trackHash ?: "",
            playbackState = playerUiState.playbackState,
            homeDir = homeDir,
            foldersAndTracksState = foldersAndTracksState,
            navPaths = navPaths,
            baseUrl = baseUrl ?: "",
            onClickNavPath = { folder ->
                foldersViewModel.onFolderUiEvent(FolderUiEvent.OnClickNavPath(folder))
            },
            onRetry = { event ->
                foldersViewModel.onFolderUiEvent(FolderUiEvent.OnRetry(event))
                mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnRetry)
            },
            onClickFolder = { folder ->
                foldersViewModel.onFolderUiEvent(FolderUiEvent.OnClickFolder(folder))
            },
            onGoToTrackAlbum = { albumHash ->
                albumWithInfoViewModel.onAlbumWithInfoUiEvent(
                    AlbumWithInfoUiEvent.OnUpdateAlbumHash(albumHash)
                )
                albumNavigator.gotoAlbumWithInfo(albumHash)
            },
            onClickTrackItem = { index: Int, queue: List<Track> ->
                mediaControllerViewModel.onQueueEvent(
                    QueueEvent.RecreateQueue(
                        source = QueueSource.FOLDER(
                            path = currentFolder.path,
                            name = currentFolder.name
                        ),
                        clickedTrackIndex = index,
                        queue = queue
                    )
                )
            }
        )

        val overrideSystemBackNav = currentFolder.path != "\$home"
        BackHandler(enabled = overrideSystemBackNav) {
            foldersViewModel.onFolderUiEvent(FolderUiEvent.OnBackNav(currentFolder))
        }
    }
}
