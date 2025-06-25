package com.android.swingmusic.folder.presentation.screen

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.swingmusic.album.presentation.event.AlbumWithInfoUiEvent
import com.android.swingmusic.album.presentation.viewmodel.AlbumWithInfoViewModel
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.BottomSheetItemModel
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.BottomSheetAction
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.state.FoldersWithPagingTracksState
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.CustomTrackBottomSheet
import com.android.swingmusic.uicomponent.presentation.component.FolderItem
import com.android.swingmusic.uicomponent.presentation.component.PathIndicatorItem
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FoldersAndTracksPaginated(
    currentFolder: Folder,
    homeDir: Folder,
    foldersWithPagingTracksState: FoldersWithPagingTracksState,
    currentTrackHash: String,
    playbackState: PlaybackState,
    navPaths: List<Folder>,
    onClickNavPath: (Folder) -> Unit,
    onRetry: (FolderUiEvent) -> Unit,
    onPullToRefresh: (FolderUiEvent) -> Unit,
    onClickFolder: (Folder) -> Unit,
    onClickTrackItem: (index: Int, queue: List<Track>) -> Unit,
    onToggleTrackFavorite: (trackHash: String, isFavorite: Boolean) -> Unit,
    onGetSheetAction: (track: Track, sheetAction: BottomSheetAction) -> Unit,
    onGotoArtist: (hash: String) -> Unit,
    baseUrl: String
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showTrackBottomSheet by remember { mutableStateOf(false) }
    var clickedTrack: Track? by remember { mutableStateOf(null) }

    val pagingTracks = foldersWithPagingTracksState.pagingTracks.collectAsLazyPagingItems()
    
    LaunchedEffect(currentFolder) {
        pagingTracks.refresh()
    }
    
    LaunchedEffect(pagingTracks.itemSnapshotList.items) {
        clickedTrack?.let { track ->
            val updatedTrack = pagingTracks.itemSnapshotList.items.find { it?.trackHash == track.trackHash }
            clickedTrack = updatedTrack ?: track
        }
        if (clickedTrack == null) showTrackBottomSheet = false
    }

    var isManualRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

    val lazyColumnState = rememberLazyListState()
    val pathsLazyRowState = rememberLazyListState()
    Scaffold { it ->
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isManualRefreshing,
            state = refreshState,
            onRefresh = {
                isManualRefreshing = true
                pagingTracks.refresh()
                val event = FolderUiEvent.OnClickFolder(currentFolder)
                onPullToRefresh(event)
            },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier
                        .padding(top = 76.dp)
                        .align(Alignment.TopCenter),
                    isRefreshing = isManualRefreshing,
                    state = refreshState
                )
            }
        ) {
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
                    LaunchedEffect(key1 = currentFolder) {
                        val index = navPaths.indexOf(currentFolder)
                        pathsLazyRowState.animateScrollToItem(if (index >= 0) index else 0)
                    }

                    if (showTrackBottomSheet) {
                        clickedTrack?.let { track ->
                            CustomTrackBottomSheet(
                                scope = scope,
                                sheetState = sheetState,
                                clickedTrack = track,
                                baseUrl = baseUrl,
                                isFavorite = track.isFavorite,
                                bottomSheetItems = listOf(
                                    BottomSheetItemModel(
                                        label = "Go to Artist",
                                        enabled = true,
                                        painterId = R.drawable.ic_artist,
                                        track = track,
                                        sheetAction = BottomSheetAction.OpenArtistsDialog(track.trackArtists)
                                    ),
                                    BottomSheetItemModel(
                                        label = "Go to Album",
                                        painterId = R.drawable.ic_album,
                                        track = track,
                                        sheetAction = BottomSheetAction.GotoAlbum
                                    ),
                                    BottomSheetItemModel(
                                        label = "Play Next",
                                        enabled = true,
                                        painterId = R.drawable.play_next,
                                        track = track,
                                        sheetAction = BottomSheetAction.PlayNext
                                    ),
                                    BottomSheetItemModel(
                                        label = "Add to playing queue",
                                        painterId = R.drawable.add_to_queue,
                                        track = track,
                                        sheetAction = BottomSheetAction.AddToQueue
                                    )
                                ),
                                onHideBottomSheet = {
                                    showTrackBottomSheet = it
                                },
                                onClickSheetItem = { sheetTrack, sheetAction ->
                                    onGetSheetAction(sheetTrack, sheetAction)
                                },
                                onChooseArtist = { hash ->
                                    onGotoArtist(hash)
                                },
                                onToggleTrackFavorite = { trackHash, isFavorite ->
                                    clickedTrack = clickedTrack?.copy(isFavorite = !isFavorite)
                                    onToggleTrackFavorite(trackHash, isFavorite)
                                }
                            )
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
                                        val tint =
                                            if (navPaths[index + 1].path == currentFolder.path)
                                                MaterialTheme.colorScheme.onSurface else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = .30F)
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            tint = tint,
                                            contentDescription = "Arrow Right"
                                        )
                                    }
                                }
                            }
                        }

                        when (val foldersResource = foldersWithPagingTracksState.folders) {
                            is Resource.Success -> {
                                itemsIndexed(
                                    items = foldersResource.data ?: emptyList(),
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
                                    
                                    if (index == (foldersResource.data?.size ?: 0) - 1 && pagingTracks.itemCount == 0) {
                                        Spacer(modifier = Modifier.height(200.dp))
                                    }
                                }
                            }
                            is Resource.Error -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = foldersResource.message ?: "Error loading folders",
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
                            is Resource.Loading -> {
                                if (pagingTracks.itemCount == 0 && pagingTracks.loadState.refresh !is LoadState.Loading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(strokeCap = StrokeCap.Round)
                                        }
                                    }
                                }
                            }
                        }

                        items(
                            count = pagingTracks.itemCount,
                            key = { index -> pagingTracks[index]?.filepath ?: "track_$index" }
                        ) { index ->
                            val track = pagingTracks[index]
                            if (track != null) {
                                TrackItem(
                                    track = track,
                                    showMenuIcon = true,
                                    isCurrentTrack = track.trackHash == currentTrackHash,
                                    playbackState = playbackState,
                                    baseUrl = baseUrl,
                                    onClickTrackItem = {
                                        val tracksList = (0 until pagingTracks.itemCount).mapNotNull { i ->
                                            pagingTracks[i]
                                        }
                                        onClickTrackItem(index, tracksList)
                                    },
                                    onClickMoreVert = { clickTrack ->
                                        clickedTrack = clickTrack
                                        showTrackBottomSheet = true
                                    }
                                )

                                if (index == pagingTracks.itemCount - 1) {
                                    Spacer(modifier = Modifier.height(200.dp))
                                }
                            }
                        }

                        when (val appendState = pagingTracks.loadState.append) {
                            is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(strokeCap = StrokeCap.Round)
                                    }
                                }
                            }
                            is LoadState.Error -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = appendState.error.message ?: "Error loading more tracks",
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(onClick = { pagingTracks.retry() }) {
                                                Text("RETRY")
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }

                        when (val refresh = pagingTracks.loadState.refresh) {
                            is LoadState.Loading -> {
                                if (pagingTracks.itemCount == 0 && 
                                    foldersWithPagingTracksState.folders !is Resource.Loading && !isManualRefreshing) {
                                    item {
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
                            }
                            is LoadState.Error -> {
                                isManualRefreshing = false
                                if (pagingTracks.itemCount == 0) {
                                    item {
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
                                                Text(
                                                    text = refresh.error.message?.replaceFirstChar {
                                                        it.titlecase(Locale.ROOT)
                                                    } ?: "Error loading tracks",
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Button(onClick = { pagingTracks.retry() }) {
                                                    Text("RETRY")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            is LoadState.NotLoading -> {
                                isManualRefreshing = false
                            }
                            else -> {}
                        }

                        if (pagingTracks.itemCount == 0 &&
                            pagingTracks.loadState.refresh !is LoadState.Loading &&
                            foldersWithPagingTracksState.folders is Resource.Success &&
                            foldersWithPagingTracksState.folders.data?.isEmpty() == true
                        ) {
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
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination
@Composable
fun FoldersAndTracksPaginatedScreen(
    foldersViewModel: FoldersViewModel,
    albumWithInfoViewModel: AlbumWithInfoViewModel = hiltViewModel(),
    mediaControllerViewModel: MediaControllerViewModel,
    navigator: CommonNavigator,
    gotoFolderName: String? = null,
    gotoFolderPath: String? = null
) {
    val currentFolder by remember { foldersViewModel.currentFolder }
    val foldersWithPagingTracksState by remember { foldersViewModel.foldersWithPagingTracks }
    val navPaths by remember { foldersViewModel.navPaths }
    val homeDir = remember { foldersViewModel.homeDir }

    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()
    
    val currentTrackHash = playerUiState.nowPlayingTrack?.trackHash ?: ""
    val playbackState = playerUiState.playbackState

    BackHandler {
        if (navPaths.size > 1) {
            foldersViewModel.onFolderUiEvent(FolderUiEvent.OnBackNav(currentFolder))
        } else {
            navigator.navigateBack()
        }
    }

    LaunchedEffect(key1 = gotoFolderName, key2 = gotoFolderPath) {
        if (!gotoFolderName.isNullOrBlank() && !gotoFolderPath.isNullOrBlank()) {
            val folder = Folder(
                name = gotoFolderName,
                path = gotoFolderPath,
                isSym = false,
                folderCount = 0,
                trackCount = 0
            )

            foldersViewModel.onFolderUiEvent(FolderUiEvent.OnClickFolder(folder))
        }
    }

    FoldersAndTracksPaginated(
        currentFolder = currentFolder,
        homeDir = homeDir,
        foldersWithPagingTracksState = foldersWithPagingTracksState,
        currentTrackHash = currentTrackHash,
        playbackState = playbackState,
        navPaths = navPaths,
        onClickNavPath = { folder ->
            foldersViewModel.onFolderUiEvent(FolderUiEvent.OnClickNavPath(folder))
        },
        onRetry = { event ->
            foldersViewModel.onFolderUiEvent(event)
        },
        onPullToRefresh = { event ->
            foldersViewModel.onFolderUiEvent(event)
        },
        onClickFolder = { folder ->
            foldersViewModel.onFolderUiEvent(FolderUiEvent.OnClickFolder(folder))
        },
        onClickTrackItem = { index, queue ->
            mediaControllerViewModel.onQueueEvent(
                QueueEvent.RecreateQueue(
                    source = QueueSource.FOLDER(
                        path = currentFolder.path,
                        name = currentFolder.name
                    ),
                    queue = queue,
                    clickedTrackIndex = index
                )
            )
        },
        onToggleTrackFavorite = { trackHash, isFavorite ->
            foldersViewModel.onFolderUiEvent(
                FolderUiEvent.ToggleTrackFavorite(
                    trackHash,
                    isFavorite
                )
            )
        },
        onGetSheetAction = { track, sheetAction ->
            when (sheetAction) {
                is BottomSheetAction.GotoAlbum -> {
                    albumWithInfoViewModel.onAlbumWithInfoUiEvent(
                        AlbumWithInfoUiEvent.OnLoadAlbumWithInfo(
                            track.albumHash
                        )
                    )
                    navigator.gotoAlbumWithInfo(track.albumHash)
                }

                is BottomSheetAction.AddToQueue -> {
                    mediaControllerViewModel.onQueueEvent(
                        QueueEvent.AddToQueue(
                            track = track,
                            source = QueueSource.FOLDER(
                                path = currentFolder.path,
                                name = currentFolder.name
                            )
                        )
                    )
                }

                is BottomSheetAction.PlayNext -> {
                    mediaControllerViewModel.onQueueEvent(
                        QueueEvent.PlayNext(
                            track = track,
                            source = QueueSource.FOLDER(
                                path = currentFolder.path,
                                name = currentFolder.name
                            )
                        )
                    )
                }

                else -> {}
            }
        },
        onGotoArtist = { hash ->
            navigator.gotoArtistInfo(hash)
        },
        baseUrl = baseUrl ?: ""
    )
}