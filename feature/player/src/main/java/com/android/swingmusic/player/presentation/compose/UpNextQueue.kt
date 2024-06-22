package com.android.swingmusic.player.presentation.compose

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.SoundSignalBars
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
private fun UpNextQueue(
    playingTrackIndex: Int,
    playbackState: PlaybackState,
    baseUrl: String,
    queue: List<Track>,
    onClickQueueItem: (index: Int) -> Unit
) {
    if (queue.isEmpty()) {
        SwingMusicTheme(navBarColor = MaterialTheme.colorScheme.surface) {
            Surface {
                Column {
                    Text(
                        modifier = Modifier.padding(
                            top = 16.dp,
                            bottom = 16.dp,
                            start = 16.dp,
                        ),
                        text = "Now Playing",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No queued tracks found!",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Tracks in queue will be shown here.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
                            )
                        }
                    }
                }
            }
        }
        return
    }

    val playingTrack = queue[playingTrackIndex]
    val lazyColumnState = rememberLazyListState()

    LaunchedEffect(key1 = true) {
        if (queue.isNotEmpty() && playingTrackIndex in queue.indices)
            lazyColumnState.animateScrollToItem(playingTrackIndex)
    }

    SwingMusicTheme(navBarColor = MaterialTheme.colorScheme.inverseOnSurface) {
        Scaffold(
            topBar = {
                Text(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        bottom = 16.dp,
                        start = 16.dp,
                    ),
                    text = "Now Playing",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        ) { paddingValues ->
            // Now Playing Track
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
                    .clickable {
                        onClickQueueItem(playingTrackIndex)
                    }
                    .padding(
                        start = 4.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp)
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
                                .size(56.dp)
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F),
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

            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 104.dp) // The exact height of now playing item plus padding.
            ) {
                itemsIndexed(
                    items = queue,
                    key = { index: Int, track: Track -> "$index" + track.filepath }
                ) { index, track ->
                    TrackItem(
                        track = track,
                        playbackState = playbackState,
                        isCurrentTrack = index == playingTrackIndex,
                        baseUrl = baseUrl,
                        onClickTrackItem = {
                            onClickQueueItem(index)
                        },
                        onClickMoreVert = {}
                    )
                }
            }
        }
    }
}

/**
 * A Composable that ties [UpNextQueue] to [MediaControllerViewModel] where its sates are hoisted
 * */

@Composable
fun UpNextQueueScreen(mediaControllerViewModel: MediaControllerViewModel = viewModel()) {
    val playerUiState by remember { mediaControllerViewModel.playerUiState }

    val baseUrl by remember { mediaControllerViewModel.baseUrl() }

    UpNextQueue(
        playingTrackIndex = playerUiState.playingTrackIndex,
        playbackState = playerUiState.playbackState,
        queue = playerUiState.queue,
        baseUrl = baseUrl ?: "",
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

    SwingMusicTheme {
        UpNextQueue(
            playingTrackIndex = 1,
            playbackState = PlaybackState.PLAYING,
            queue = queue,
            baseUrl = "",
            onClickQueueItem = { _: Int -> },
        )
    }
}
