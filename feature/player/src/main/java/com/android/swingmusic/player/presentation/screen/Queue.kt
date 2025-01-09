package com.android.swingmusic.player.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.BottomSheetItemModel
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.BottomSheetAction
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.util.navigateToSource
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.CustomTrackBottomSheet
import com.android.swingmusic.uicomponent.presentation.component.SoundSignalBars
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.android.swingmusic.uicomponent.presentation.util.BlurTransformation
import com.android.swingmusic.uicomponent.presentation.util.getName
import com.android.swingmusic.uicomponent.presentation.util.getSourceType
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Queue(
    queue: List<Track>,
    source: QueueSource,
    playingTrackIndex: Int,
    playingTrack: Track?,
    playbackState: PlaybackState,
    baseUrl: String,
    onClickQueueSource: (source: QueueSource) -> Unit,
    onToggleTrackFavorite: (trackHash: String, isFavorite: Boolean) -> Unit,
    onTogglePlayerState: () -> Unit,
    onClickQueueItem: (index: Int) -> Unit,
    onGetSheetAction: (track: Track, sheetAction: BottomSheetAction) -> Unit,
    onGotoArtist: (hash: String) -> Unit,
) {
    val lazyColumnState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showTrackBottomSheet by remember { mutableStateOf(false) }
    var clickedTrack: Track? by remember { mutableStateOf(null) }

    LaunchedEffect(queue) {
        clickedTrack?.let { track ->
            val updatedTrack = queue.find { it.trackHash == track.trackHash }
            clickedTrack = updatedTrack ?: track
        }
    }

    // scroll past the playing track by one item to keep highlighted cards far apart at first
    LaunchedEffect(key1 = Unit) {
        if ((playingTrackIndex - 1) in queue.indices) {
            lazyColumnState.scrollToItem((playingTrackIndex - 1))
        }
    }

    Scaffold { outerPadding ->
        if (showTrackBottomSheet) {
            clickedTrack?.let { track ->
                CustomTrackBottomSheet(
                    scope = scope,
                    sheetState = sheetState,
                    clickedTrack = track,
                    isFavorite = track.isFavorite,
                    baseUrl = baseUrl,
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
                            label = "Go to Folder",
                            enabled = true,
                            painterId = R.drawable.folder_outlined_open,
                            track = track,
                            sheetAction = BottomSheetAction.GotoFolder(
                                name = track.folder.getFolderName(),
                                path = track.folder
                            )
                        ),
                        BottomSheetItemModel(
                            label = "Play Next",
                            enabled = true,
                            painterId = R.drawable.play_next,
                            track = track,
                            sheetAction = BottomSheetAction.PlayNext
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
                        onToggleTrackFavorite(trackHash, isFavorite)
                    }
                )
            }
        }

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1F),
            model = ImageRequest.Builder(LocalContext.current)
                .data("${baseUrl}img/thumbnail/${playingTrack?.image}")
                .crossfade(true)
                .transformations(
                    listOf(
                        BlurTransformation(
                            scale = 0.25f,
                            radius = 25
                        )
                    )
                )
                .build(),
            contentDescription = "Track Image",
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1F)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = .75F),
                            MaterialTheme.colorScheme.surface.copy(alpha = .95F),
                            MaterialTheme.colorScheme.surface.copy(alpha = 1F),
                            MaterialTheme.colorScheme.surface.copy(alpha = 1F),
                            MaterialTheme.colorScheme.surface.copy(alpha = 1F)
                        )
                    )
                )
        )

        Scaffold(
            modifier = Modifier
                .padding(outerPadding)
                .fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 4.dp,
                            top = 16.dp,
                            start = 12.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }, bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        ) { paddingValues ->
            if (playingTrack != null) {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .clickable(
                                indication = null,
                                interactionSource = interactionSource
                            ) {
                                onClickQueueSource(source)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = source.getSourceType(),
                            style = TextStyle(
                                fontSize = 11.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .94F)
                        )

                        if (source.getName().isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .36F))
                            )

                            Text(
                                text = source.getName(),
                                style = TextStyle(
                                    fontSize = 11.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .94F)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .14F))
                            .clickable {
                                onTogglePlayerState()
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.8F),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data("${baseUrl}img/thumbnail/small/${playingTrack.image}")
                                        .crossfade(true)
                                        .build(),
                                    placeholder = painterResource(R.drawable.audio_fallback),
                                    fallback = painterResource(R.drawable.audio_fallback),
                                    error = painterResource(R.drawable.audio_fallback),
                                    contentDescription = "Track Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )

                                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(
                                        text = playingTrack.title,
                                        modifier = Modifier.sizeIn(maxWidth = 300.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val artists =
                                            playingTrack.trackArtists.joinToString(", ") { it.name }

                                        Text(
                                            text = artists,
                                            modifier = Modifier.sizeIn(maxWidth = 185.dp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = .80F
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Sound Bars
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(end = 8.dp)
                            ) {
                                if (playbackState == PlaybackState.PLAYING) {
                                    SoundSignalBars(animate = true)
                                } else {
                                    SoundSignalBars(animate = false)
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier
                    .background(Color.Transparent)
                    .padding(paddingValues)
                    .padding(top = 128.dp)
                    .fillMaxSize()
            ) {
                if (playingTrack == null) {
                    item {}
                } else {
                    itemsIndexed(
                        items = queue,
                        key = { index: Int, track: Track -> "$index:${track.filepath}" }
                    ) { index, track ->
                        TrackItem(
                            track = track,
                            playbackState = playbackState,
                            isCurrentTrack = index == playingTrackIndex,
                            baseUrl = baseUrl,
                            showMenuIcon = true,
                            onClickTrackItem = {
                                onClickQueueItem(index)
                            },
                            onClickMoreVert = {
                                clickedTrack = it
                                showTrackBottomSheet = true
                            }
                        )

                        if (index == queue.lastIndex) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A Composable that ties [Queue] to [MediaControllerViewModel] where its sates are hoisted
 * */

@Destination
@Composable
fun QueueScreen(
    mediaControllerViewModel: MediaControllerViewModel,
    navigator: CommonNavigator
) {
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    Queue(
        queue = playerUiState.queue,
        source = playerUiState.source,
        playingTrackIndex = playerUiState.playingTrackIndex,
        playingTrack = playerUiState.nowPlayingTrack,
        playbackState = playerUiState.playbackState,
        baseUrl = baseUrl ?: "",
        onClickQueueSource = { source ->
            source.navigateToSource(navigator)
        },
        onTogglePlayerState = { mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnTogglePlayerState) },
        onClickQueueItem = { index: Int ->
            mediaControllerViewModel.onQueueEvent(QueueEvent.SeekToQueueItem(index))
        },
        onGetSheetAction = { track, sheetAction ->
            when (sheetAction) {
                is BottomSheetAction.GotoAlbum -> {
                    navigator.gotoAlbumWithInfo(track.albumHash)
                }

                is BottomSheetAction.GotoFolder -> {
                    navigator.gotoSourceFolder(name = sheetAction.name, path = sheetAction.path)
                }

                is BottomSheetAction.PlayNext -> {
                    mediaControllerViewModel.onQueueEvent(
                        QueueEvent.PlayNext(
                            track = track,
                            source = playerUiState.source
                        )
                    )
                }

                else -> {}
            }
        },
        onGotoArtist = { hash ->
            navigator.gotoArtistInfo(artistHash = hash)
        },
        onToggleTrackFavorite = { isFavorite, trackHash ->
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnToggleFavorite(trackHash, isFavorite)
            )
        }
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun QueuePreview() {
    val juice = TrackArtist(
        artistHash = "juice123",
        image = "juice.jpg",
        name = "Juice Wrld"
    )

    val albumArtists = listOf(juice)
    val artists = listOf(juice)

    val track = Track(
        album = "Sample Album",
        albumTrackArtists = albumArtists,
        albumHash = "albumHash123",
        trackArtists = artists,
        bitrate = 320,
        duration = 454, // Sample duration in seconds
        filepath = "/path/to/track.mp3",
        folder = "/path/to/album",
        image = "/path/to/album/artwork.jpg",
        isFavorite = true,
        title = "All Girls are the same",
        trackHash = "trackHash123",
        disc = 1,
        trackNumber = 1
    )

    val queue = mutableListOf(
        track,
        track.copy(title = "Popular", trackHash = "popular"),
        track.copy(title = "One Right Now", trackHash = "one")
    )

    SwingMusicTheme_Preview {
        Queue(
            playingTrackIndex = 0,
            source = QueueSource.ALBUM("hash", "Sample Khalid Album"),
            playingTrack = track,
            playbackState = PlaybackState.PLAYING,
            queue = queue,
            baseUrl = "",
            onClickQueueSource = {},
            onTogglePlayerState = {},
            onClickQueueItem = {},
            onGetSheetAction = { _, _ -> },
            onGotoArtist = {},
            onToggleTrackFavorite = { _, _ -> }
        )
    }
}

internal fun String.getFolderName(): String {
    val sanitizedPath = this.trimEnd('/')
    return sanitizedPath.substringAfterLast('/')
}
