package com.android.swingmusic.search.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.swingmusic.artist.presentation.event.ArtistInfoUiEvent
import com.android.swingmusic.artist.presentation.viewmodel.ArtistInfoViewModel
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.BottomSheetItemModel
import com.android.swingmusic.core.domain.model.TopResultItem
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.BottomSheetAction
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.search.presentation.event.SearchUiEvent
import com.android.swingmusic.search.presentation.util.isScrollingUp
import com.android.swingmusic.search.presentation.viewmodel.SearchViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.AlbumItem
import com.android.swingmusic.uicomponent.presentation.component.ArtistItem
import com.android.swingmusic.uicomponent.presentation.component.CustomTrackBottomSheet
import com.android.swingmusic.uicomponent.presentation.component.TopSearchResultItem
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.Screen
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun Search(
    isLoading: Boolean,
    isLoadingTopItemTracks: Boolean,
    hasSearched: Boolean,
    isError: Boolean,
    errorMessage: String?,
    baseUrl: String,
    snackbarHostState: SnackbarHostState,
    searchParams: String,
    playingTrackHash: String,
    playbackState: PlaybackState,
    topResultItem: TopResultItem?,
    topSearchTracks: List<Track>,
    albumSearchResults: List<Album>,
    artistsSearchResults: List<Artist>,
    onSearchParamChanged: (params: String) -> Unit,
    onRetrySearch: () -> Unit,
    onClickTopResultItem: (type: String, hash: String) -> Unit,
    onClickPlayTopResultItem: (type: String, hash: String) -> Unit,
    onClickTrackItem: (queue: List<Track>, index: Int) -> Unit,
    onClickAlbumItem: (hash: String) -> Unit,
    onClickArtist: (hash: String) -> Unit,
    onClickViewAll: (itemType: String) -> Unit,
    onClickMoreVert: (track: Track) -> Unit,
) {
    val lazyColumnState = rememberLazyListState()
    val clickInteractionSource = remember { MutableInteractionSource() }

    Scaffold {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 170.dp)
                )
            },
            modifier = Modifier.padding(it),
            topBar = {
                Column {
                    Text(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 4.dp
                        ),
                        text = "Search",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    AnimatedVisibility(visible = lazyColumnState.isScrollingUp()) {
                        TextField(
                            value = searchParams,
                            onValueChange = { newValue ->
                                onSearchParamChanged(newValue)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            placeholder = { Text("search swing music") },
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.swing_music_logo_rounded),
                                    contentDescription = "App Icon"
                                )
                            },
                            trailingIcon = {
                                if (searchParams.trim().isNotEmpty()) {
                                    IconButton(onClick = {
                                        onSearchParamChanged("")
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                isError -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = errorMessage ?: "An error has occurred!")

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(onClick = onRetrySearch) {
                                Text(text = "Retry")
                            }
                        }
                    }
                }

                (hasSearched &&
                        topResultItem == null &&
                        topSearchTracks.isEmpty() &&
                        albumSearchResults.isEmpty() &&
                        artistsSearchResults.isEmpty() &&
                        searchParams.isNotEmpty()
                        ) -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.no_match_found),
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(text = "No match found!")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = lazyColumnState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        topResultItem?.let { result ->
                            item {
                                Column(
                                    modifier = Modifier.padding(
                                        top = 16.dp,
                                        bottom = 4.dp,
                                        start = 20.dp,
                                        end = 20.dp
                                    )
                                ) {
                                    Text(
                                        text = "Top Result",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    TopSearchResultItem(
                                        baseUrl = baseUrl,
                                        isLoadingTracks = isLoadingTopItemTracks,
                                        topResultItem = result,
                                        onClickTopResultItem = { type, hash ->
                                            onClickTopResultItem(
                                                type,
                                                hash
                                            )
                                        },
                                        onClickPlayTopResultItem = { type, hash ->
                                            if (type == "track") onClickTopResultItem(
                                                "track",
                                                hash
                                            ) else onClickPlayTopResultItem(
                                                type,
                                                hash
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        if (topSearchTracks.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .fillParentMaxWidth()
                                        .padding(
                                            top = 24.dp,
                                            bottom = 4.dp,
                                            start = 20.dp,
                                            end = 20.dp
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tracks",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )

                                    // server returns a static number (4)
                                    if (topSearchTracks.size > 3) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .clickable(
                                                    interactionSource = clickInteractionSource,
                                                    indication = null
                                                ) {
                                                    onClickViewAll("tracks")
                                                }
                                        ) {
                                            Text(
                                                text = "View All",
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = .9F
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            itemsIndexed(topSearchTracks.take(4)) { index, track ->
                                TrackItem(
                                    track = track,
                                    showMenuIcon = true,
                                    playbackState = playbackState,
                                    isCurrentTrack = track.trackHash == playingTrackHash,
                                    onClickTrackItem = {
                                        onClickTrackItem(
                                            topSearchTracks,
                                            index
                                        )
                                    },
                                    onClickMoreVert = { clicked ->
                                        onClickMoreVert(clicked)
                                    },
                                    baseUrl = baseUrl
                                )
                            }
                        }


                        if (albumSearchResults.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .fillParentMaxWidth()
                                        .padding(
                                            top = 24.dp,
                                            bottom = 4.dp,
                                            start = 20.dp,
                                            end = 20.dp
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Albums",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )

                                    if (albumSearchResults.size > 4) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .clickable(
                                                    interactionSource = clickInteractionSource,
                                                    indication = null
                                                ) {
                                                    onClickViewAll("albums")
                                                }
                                        ) {
                                            Text(
                                                text = "View All",
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = .9F
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                LazyRow(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .padding(horizontal = 12.dp)
                                ) {
                                    items(albumSearchResults.take(4)) { album ->
                                        Box(modifier = Modifier.width(170.dp)) {
                                            AlbumItem(
                                                modifier = Modifier.fillMaxWidth(),
                                                screen = Screen.SEARCH,
                                                albumArtistHash = album.albumArtists.firstOrNull()?.artistHash
                                                    ?: "hash",
                                                album = album,
                                                baseUrl = baseUrl,
                                                onClick = { albumHash ->
                                                    onClickAlbumItem(albumHash)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (artistsSearchResults.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .fillParentMaxWidth()
                                        .padding(
                                            top = 16.dp,
                                            bottom = 4.dp,
                                            start = 20.dp,
                                            end = 20.dp
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Artists",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )

                                    if (artistsSearchResults.size > 4) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .clickable(
                                                    interactionSource = clickInteractionSource,
                                                    indication = null
                                                ) {
                                                    onClickViewAll("artists")
                                                }
                                        ) {
                                            Text(
                                                text = "View All",
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = .9F
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                LazyRow(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .padding(horizontal = 12.dp)
                                ) {
                                    items(items = artistsSearchResults.take(4)) { artist ->
                                        Box(modifier = Modifier.width(170.dp)) {
                                            ArtistItem(
                                                modifier = Modifier.fillMaxWidth(),
                                                artist = artist,
                                                baseUrl = baseUrl,
                                                onClick = { artistHash ->
                                                    onClickArtist(artistHash)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(250.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    mediaControllerViewModel: MediaControllerViewModel,
    navigator: CommonNavigator,
    artistInfoViewModel: ArtistInfoViewModel
) {
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()
    val playerState by mediaControllerViewModel.playerUiState.collectAsState()

    val searchUiState by searchViewModel.searchState.collectAsState()

    val topResultItem = searchUiState.topSearchResults.topResultItem
    val tracks = searchUiState.topSearchResults.tracks
    val albums = searchUiState.topSearchResults.albums
    val artists = searchUiState.topSearchResults.artists

    val topItemTracks = searchUiState.topItemTracks

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTrackBottomSheet by remember { mutableStateOf(false) }
    var clickedTrack: Track? by remember { mutableStateOf(null) }

    LaunchedEffect(tracks) {
        clickedTrack?.let { track ->
            val updatedTrack = tracks.find { it.trackHash == track.trackHash }
            clickedTrack = updatedTrack ?: track
        }
    }

    LaunchedEffect(key1 = topItemTracks) {
        if (topItemTracks.isNullOrEmpty().not() && topResultItem != null) {
            val source = when (topResultItem.type) {
                "artist" -> {
                    val artist = topResultItem.artists.firstOrNull()
                    artist?.let {
                        QueueSource.ARTIST(it.artistHash, it.name)
                    }
                }

                "album" -> {
                    val albumHash = topResultItem.albumHash
                    val albumName = topResultItem.album
                    QueueSource.ALBUM(albumHash, albumName)
                }

                else -> null
            }

            if (mediaControllerViewModel.playerUiState.value.source != source) {
                mediaControllerViewModel.onQueueEvent(
                    QueueEvent.RecreateQueue(
                        source = source ?: QueueSource.SEARCH,
                        clickedTrackIndex = 0,
                        queue = topItemTracks!!
                    )
                )
            }
        }
    }


    SwingMusicTheme {
        Surface {
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

            Search(
                isLoading = searchUiState.isLoadingTopResult,
                isError = searchUiState.isError,
                hasSearched = searchUiState.hasSearched,
                isLoadingTopItemTracks = searchUiState.isLoadingTopItemTracks,
                errorMessage = searchUiState.errorMessage,
                baseUrl = baseUrl ?: "https://default",
                snackbarHostState = snackbarHostState,
                searchParams = searchUiState.searchParams,
                playingTrackHash = playerState.nowPlayingTrack?.trackHash ?: "hash",
                playbackState = playerState.playbackState,
                topResultItem = topResultItem,
                topSearchTracks = tracks,
                albumSearchResults = albums,
                artistsSearchResults = artists,
                onSearchParamChanged = {
                    searchViewModel.onSearchUiEvent(
                        SearchUiEvent.OnSearchParamChanged(it)
                    )
                },
                onRetrySearch = {
                    searchViewModel.onSearchUiEvent(
                        SearchUiEvent.OnRetrySearchTopResults
                    )
                },
                onClickPlayTopResultItem = { type, hash ->
                    when (type) {
                        "artist" -> {
                            searchViewModel.onSearchUiEvent(
                                SearchUiEvent.OnGetArtistTracks(hash)
                            )
                        }

                        "album" -> {
                            searchViewModel.onSearchUiEvent(
                                SearchUiEvent.OnGetAlbumTacks(hash)
                            )
                        }
                    }
                },
                onClickTopResultItem = { type, hash ->
                    when (type) {
                        "artist" -> navigator.gotoArtistInfo(hash)
                        "album" -> navigator.gotoAlbumWithInfo(hash)
                        "track" -> {
                            val trackArtists = topResultItem?.artists?.map {
                                TrackArtist(
                                    name = it.name,
                                    artistHash = it.artistHash,
                                    image = it.image
                                )
                            } ?: emptyList()

                            val track = Track(
                                album = topResultItem?.album ?: "",
                                albumTrackArtists = trackArtists,
                                albumHash = topResultItem?.albumHash ?: "",
                                trackArtists = trackArtists,
                                bitrate = topResultItem?.bitrate ?: 0,
                                duration = topResultItem?.duration ?: 0,
                                filepath = topResultItem?.filepath ?: "",
                                folder = topResultItem?.folder ?: "\$home",
                                image = topResultItem?.image ?: "",
                                isFavorite = topResultItem?.isFavorite ?: false,
                                title = topResultItem?.title ?: "",
                                trackHash = hash,
                                disc = 1,
                                trackNumber = 1
                            )

                            mediaControllerViewModel.onQueueEvent(
                                QueueEvent.RecreateQueue(
                                    source = QueueSource.SEARCH,
                                    clickedTrackIndex = 0,
                                    queue = listOf(track)
                                )
                            )
                        }
                    }
                },
                onClickTrackItem = { queue, index ->
                    mediaControllerViewModel.onQueueEvent(
                        QueueEvent.RecreateQueue(
                            source = QueueSource.SEARCH,
                            clickedTrackIndex = index,
                            queue = queue
                        )
                    )
                },
                onClickAlbumItem = {
                    navigator.gotoAlbumWithInfo(it)
                },
                onClickViewAll = { viewAllType ->
                    val searchParams = searchUiState.viewAllSearchParam
                    navigator.gotoViewAllSearchResultsScreen(viewAllType, searchParams)
                },
                onClickArtist = {
                    navigator.gotoArtistInfo(it)
                },
                onClickMoreVert = { track ->
                    clickedTrack = track
                    showTrackBottomSheet = true
                }
            )
        }
    }
}
