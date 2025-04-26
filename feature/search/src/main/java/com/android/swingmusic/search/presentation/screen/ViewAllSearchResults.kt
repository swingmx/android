package com.android.swingmusic.search.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.swingmusic.artist.presentation.event.ArtistInfoUiEvent
import com.android.swingmusic.artist.presentation.viewmodel.ArtistInfoViewModel
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.BottomSheetItemModel
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.BottomSheetAction
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.search.presentation.event.SearchUiEvent
import com.android.swingmusic.search.presentation.viewmodel.SearchViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.AlbumItem
import com.android.swingmusic.uicomponent.presentation.component.ArtistItem
import com.android.swingmusic.uicomponent.presentation.component.CustomTrackBottomSheet
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ViewAllSearchResults(
    searchParams: String,
    viewAllType: String,
    navigator: CommonNavigator,
    mediaControllerViewModel: MediaControllerViewModel,
    searchViewModel: SearchViewModel = hiltViewModel(),
    artistInfoViewModel: ArtistInfoViewModel = hiltViewModel()
) {
    val searchState by searchViewModel.searchState.collectAsState()
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    val allTracks = searchState.viewAllTracks
    val allAlbums = searchState.viewAllAlbums
    val allArtists = searchState.viewAllArtists

    val isLoading = listOf(allTracks, allAlbums, allArtists).any { it is Resource.Loading }
    val isError = listOf(allTracks, allAlbums, allArtists).any { it is Resource.Error }

    val gridState = rememberLazyGridState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTrackBottomSheet by remember { mutableStateOf(false) }
    var clickedTrack: Track? by remember { mutableStateOf(null) }

    LaunchedEffect(allTracks) {
        clickedTrack?.let { track ->
            val updatedTrack = allTracks?.data?.find { it.trackHash == track.trackHash }
            clickedTrack = updatedTrack ?: track
        }
    }

    val errorMessage = when {
        isError -> when (viewAllType) {
            "tracks" -> allTracks?.message ?: "Error"

            "albums" -> allAlbums?.message ?: "Error"

            "artists" -> allArtists?.message ?: "Error"

            else -> "Unknown Error"
        }

        else -> ""
    }

    LaunchedEffect(Unit) {
        searchViewModel.onSearchUiEvent(
            SearchUiEvent.OnClearSearchAllResources
        )

        when (viewAllType) {
            "tracks" -> {
                searchViewModel.onSearchUiEvent(
                    SearchUiEvent.OnSearchAllTacks(searchParams)
                )
            }

            "albums" -> {
                searchViewModel.onSearchUiEvent(
                    SearchUiEvent.OnSearchAllAlbums(searchParams)
                )
            }

            "artists" -> {
                searchViewModel.onSearchUiEvent(
                    SearchUiEvent.OnSearchAllArtists(searchParams)
                )
            }
        }
    }

    Scaffold { outerPadding ->
        Scaffold(
            modifier = Modifier.padding(outerPadding),
            topBar = {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 4.dp
                    )
                ) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewAllType.replaceFirstChar { it.uppercase() },
                            style = TextStyle(
                                fontSize = 11.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F)
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .36F))
                        )

                        Text(
                            text = "\"${searchParams}\"",
                            style = TextStyle(
                                fontSize = 11.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F)
                        )
                    }
                }
            }
        ) { pdValues ->
            if (showTrackBottomSheet) {
                clickedTrack?.let { track ->
                    CustomTrackBottomSheet(
                        scope = scope,
                        sheetState = sheetState,
                        isFavorite = track.isFavorite,
                        clickedTrack = track,
                        baseUrl = baseUrl ?: "",
                        currentArtisthash = null,
                        bottomSheetItems = listOf(
                            BottomSheetItemModel(
                                label = "Go to Artist",
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
                                label = "Go to Folder",
                                painterId = R.drawable.folder_outlined_open,
                                track = track,
                                sheetAction = BottomSheetAction.GotoFolder(
                                    name = track.folder.getFolderName(),
                                    path = track.folder
                                )
                            ),
                            BottomSheetItemModel(
                                label = "Play Next",
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
                            when (sheetAction) {
                                is BottomSheetAction.GotoAlbum -> {
                                    navigator.gotoAlbumWithInfo(sheetTrack.albumHash)
                                }

                                is BottomSheetAction.GotoFolder -> {
                                    navigator.gotoSourceFolder(
                                        sheetAction.name,
                                        sheetAction.path
                                    )
                                }

                                is BottomSheetAction.PlayNext -> {
                                    mediaControllerViewModel.onQueueEvent(
                                        QueueEvent.PlayNext(
                                            track = sheetTrack,
                                            source = QueueSource.SEARCH
                                        )
                                    )

                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Track added to play next",
                                            actionLabel = "View Queue",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            navigator.gotoQueueScreen()
                                        }
                                    }
                                }

                                is BottomSheetAction.AddToQueue -> {
                                    mediaControllerViewModel.onQueueEvent(
                                        QueueEvent.AddToQueue(
                                            track = sheetTrack,
                                            source = QueueSource.SEARCH
                                        )
                                    )

                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Track added to playing queue",
                                            actionLabel = "View Queue",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            navigator.gotoQueueScreen()
                                        }
                                    }
                                }

                                else -> {}
                            }
                        },
                        onChooseArtist = { hash ->
                            artistInfoViewModel.onArtistInfoUiEvent(
                                ArtistInfoUiEvent.OnLoadArtistInfo(hash)
                            )
                            navigator.gotoArtistInfo(hash)
                        },
                        onToggleTrackFavorite = { trackHash, isFavorite ->
                            searchViewModel.onSearchUiEvent(
                                SearchUiEvent.OnToggleTrackFavorite(
                                    trackHash = trackHash,
                                    isFavorite = isFavorite
                                )
                            )
                        }
                    )
                }
            }

            when {
                isError -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pdValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    when (viewAllType) {
                                        "tracks" -> {
                                            searchViewModel.onSearchUiEvent(
                                                SearchUiEvent.OnSearchAllTacks(searchParams)
                                            )
                                        }

                                        "albums" -> {
                                            searchViewModel.onSearchUiEvent(
                                                SearchUiEvent.OnSearchAllAlbums(searchParams)
                                            )
                                        }

                                        "artists" -> {
                                            searchViewModel.onSearchUiEvent(
                                                SearchUiEvent.OnSearchAllArtists(searchParams)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pdValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Loading, please wait...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                else -> {
                    when (viewAllType) {
                        "tracks" -> {
                            allTracks?.let { tracks ->
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(pdValues)
                                ) {
                                    itemsIndexed(
                                        items = tracks.data ?: emptyList(),
                                        key = { _: Int, item: Track -> item.filepath }
                                    ) { index, track ->
                                        TrackItem(
                                            track = track,
                                            showMenuIcon = true,
                                            isCurrentTrack = track.trackHash ==
                                                    (playerUiState.nowPlayingTrack?.trackHash
                                                        ?: ""),
                                            playbackState = playerUiState.playbackState,
                                            baseUrl = baseUrl ?: "",
                                            onClickTrackItem = {
                                                mediaControllerViewModel.onQueueEvent(
                                                    QueueEvent.RecreateQueue(
                                                        source = QueueSource.SEARCH,
                                                        clickedTrackIndex = index,
                                                        queue = tracks.data ?: emptyList()
                                                    )
                                                )
                                            },
                                            onClickMoreVert = { trackClicked ->
                                                clickedTrack = trackClicked
                                                showTrackBottomSheet = true
                                            }
                                        )

                                        if (index == tracks.data?.lastIndex) {
                                            Spacer(modifier = Modifier.height(100.dp))
                                        }
                                    }
                                }
                            }
                        }

                        "albums" -> {
                            allAlbums?.let { albums ->
                                LazyVerticalGrid(
                                    modifier = Modifier
                                        .padding(pdValues)
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    columns = GridCells.Fixed(2),
                                    state = gridState,
                                ) {
                                    items(
                                        items = albums.data ?: emptyList(),
                                        key = { item -> item.albumHash }
                                    ) { album ->
                                        AlbumItem(
                                            modifier = Modifier.fillMaxSize(),
                                            album = album,
                                            baseUrl = baseUrl ?: "",
                                            onClick = {
                                                navigator.gotoAlbumWithInfo(it)
                                            }
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(50.dp))
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(50.dp))
                                    }
                                }
                            }
                        }

                        "artists" -> {
                            allArtists?.let { artists ->
                                LazyVerticalGrid(
                                    modifier = Modifier
                                        .padding(pdValues)
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    columns = GridCells.Fixed(2),
                                    state = gridState,
                                ) {
                                    items(
                                        items = artists.data ?: emptyList(),
                                        key = { item -> item.artistHash }
                                    ) { artist ->
                                        ArtistItem(
                                            modifier = Modifier.fillMaxSize(),
                                            artist = artist,
                                            baseUrl = baseUrl ?: "",
                                            onClick = { artistHash ->
                                                navigator.gotoArtistInfo(artistHash)
                                            }
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(50.dp))
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(50.dp))
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

internal fun String.getFolderName(): String {
    val sanitizedPath = this.trimEnd('/')
    return sanitizedPath.substringAfterLast('/')
}
