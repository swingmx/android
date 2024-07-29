package com.android.swingmusic.player.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.SoundSignalBars
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Queue(
    queue: List<Track>,
    playingTrackIndex: Int,
    playingTrack: Track?,
    playbackState: PlaybackState,
    baseUrl: String,
    onClickBack: () -> Unit,
    onTogglePlayerState: () -> Unit,
    onClickQueueItem: (index: Int) -> Unit
) {
    val lazyColumnState = rememberLazyListState()

    LaunchedEffect(key1 = Unit) {
        if ((playingTrackIndex - 1) in queue.indices) {
            lazyColumnState.scrollToItem((playingTrackIndex - 1))
        }
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(
                            top = 48.dp,
                            bottom = 4.dp,
                            start = 16.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onClickBack() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Arrow Back"
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
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
        LazyColumn(
            state = lazyColumnState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {

            if (playingTrack == null) {
                item {

                }
            } else {
                // Now Playing Track
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp)
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

                itemsIndexed(
                    items = queue,
                    key = { index: Int, track: Track -> "$index${track.filepath}" }
                ) { index, track ->
                    TrackItem(
                        track = track,
                        playbackState = playbackState,
                        isCurrentTrack = index == playingTrackIndex,
                        trackQueueNumber = index + 1,
                        baseUrl = baseUrl,
                        onClickTrackItem = {
                            onClickQueueItem(index)
                        },
                        onClickMoreVert = {}
                    )

                    if (index == queue.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * A Composable that ties [Queue] to [MediaControllerViewModel] where its sates are hoisted
 * */

@Composable
fun QueueScreen(
    mediaControllerViewModel: MediaControllerViewModel,
    onClickBack: () -> Unit,
) {
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    Queue(
        queue = playerUiState.queue,
        playingTrackIndex = playerUiState.playingTrackIndex,
        playingTrack = playerUiState.nowPlayingTrack,
        playbackState = playerUiState.playbackState,
        baseUrl = baseUrl ?: "",
        onClickBack = { onClickBack() },
        onTogglePlayerState = { mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnTogglePlayerState) },
        onClickQueueItem = { index: Int ->
            mediaControllerViewModel.onQueueEvent(QueueEvent.SeekToQueueItem(index))
        }
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun UpNextQueuePreview() {
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
        artistHashes = "artistHashes123",
        trackArtists = artists,
        bitrate = 320,
        duration = 454, // Sample duration in seconds
        filepath = "/path/to/track.mp3",
        folder = "/path/to/album",
        image = "/path/to/album/artwork.jpg",
        isFavorite = true,
        title = "All Girls are the same",
        trackHash = "trackHash123"
    )

    val queue = mutableListOf(
        track,
        track.copy(title = "Popular", trackHash = "popular"),
        track.copy(title = "One Right Now", trackHash = "one")
    )

    SwingMusicTheme_Preview {
        Queue(
            playingTrackIndex = 1,
            playingTrack = track,
            playbackState = PlaybackState.PLAYING,
            queue = queue,
            baseUrl = "",
            onClickBack = {},
            onTogglePlayerState = {},
            onClickQueueItem = { },
        )
    }
}
