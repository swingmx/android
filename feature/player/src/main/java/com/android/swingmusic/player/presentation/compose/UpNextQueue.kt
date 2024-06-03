package com.android.swingmusic.player.presentation.compose

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
private fun UpNextQueue(
    playingTrackIndex: Int,
    playbackState: PlaybackState,
    queue: List<Track>,
    onClickUpNextTrackItem: () -> Unit,
    onClickQueueItem: (index: Int) -> Unit
) {
    if (queue.isEmpty()) {
        SwingMusicTheme {
            val view = LocalView.current
            val color = MaterialTheme.colorScheme.surface.toArgb()
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    window.navigationBarColor = color
                }
            }

            Surface {
                Column {
                    Text(
                        modifier = Modifier.padding(
                            top = 16.dp,
                            bottom = 16.dp,
                            start = 16.dp,
                        ),
                        text = "Up Next",
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
                                text = "I don't know how you got here.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
                            )
                        }
                    }
                }
            }
        }
        return
    } else {
        val view = LocalView.current
        val color = MaterialTheme.colorScheme.surface.toArgb()
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.navigationBarColor = color
            }
        }
    }

    val nextTrackIndex = when {
        playingTrackIndex == queue.lastIndex -> 0
        playingTrackIndex > queue.lastIndex -> 0
        playingTrackIndex < 0 -> 0
        else -> playingTrackIndex + 1
    }
    val upNextTrack = queue[nextTrackIndex]
    val lazyColumnState = rememberLazyListState()

    LaunchedEffect(key1 = true) {
        if (queue.isNotEmpty() && playingTrackIndex in queue.indices)
            lazyColumnState.animateScrollToItem(playingTrackIndex)
    }

    SwingMusicTheme {
        Scaffold(
            topBar = {
                Text(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        bottom = 16.dp,
                        start = 16.dp,
                    ),
                    text = "Up Next",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        ) { paddingValues ->
            // UpNext Track
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
                    .fillMaxWidth()
                    /*.border(
                        width = (0.4).dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(16.dp)
                    )*/
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
                    .clickable {
                        onClickUpNextTrackItem()
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
                    modifier = Modifier.padding(
                      //  start = 12.dp
                        start = 8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("$BASE_URL/img/t/${upNextTrack.image}")
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
                            text = upNextTrack.title,
                            modifier = Modifier.sizeIn(maxWidth = 300.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var artists = ""

                            upNextTrack.trackArtists.forEachIndexed { index, trackArtist ->
                                artists += trackArtist.name

                                if (upNextTrack.trackArtists.lastIndex != index) {
                                    artists += ", "
                                }
                            }

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

                /*Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .wrapContentSize()
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (nextTrackIndex + 1).toString(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }*/
            }

            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 104.dp) // The exact height of UpNext Track item plus padding.
            ) {
                itemsIndexed(
                    items = queue,
                    key = { index: Int, track: Track -> "$index" + track.filepath }
                ) { index, track ->
                    TrackItem(
                        track = track,
                        playbackState = playbackState,
                        isCurrentTrack = index == playingTrackIndex,
                        trackQueueNumber = index + 1,
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

    UpNextQueue(
        playingTrackIndex = playerUiState.playingTrackIndex,
        playbackState = playerUiState.playbackState,
        queue = playerUiState.queue,
        onClickUpNextTrackItem = {
            if (playerUiState.playingTrackIndex == playerUiState.queue.lastIndex) {
                mediaControllerViewModel.onQueueEvent(QueueEvent.SeekToQueueItem(0))
            } else {
                mediaControllerViewModel.onQueueEvent(QueueEvent.PlaUpNextTrack)
            }
        },
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
    val weeknd = TrackArtist(
        artistHash = "juice123",
        image = "juice.jpg",
        name = "The Weeknd"
    )

    val albumArtists = listOf(weeknd)
    val artists = listOf(weeknd)
    val genre = listOf("Rap", "Emo")

    val track = Track(
        album = "Sample Album",
        albumTrackArtists = albumArtists,
        albumHash = "albumHash123",
        artistHashes = "artistHashes123",
        trackArtists = artists,
        ati = "ati123",
        bitrate = 320,
        copyright = "Copyright © 2024",
        createdDate = 1648731600.0, // Sample timestamp
        date = 2024,
        disc = 1,
        duration = 454, // Sample duration in seconds
        filepath = "/path/to/track.mp3",
        folder = "/path/to/album",
        genre = genre,
        image = "/path/to/album/artwork.jpg",
        isFavorite = true,
        lastMod = 1648731600, // Sample timestamp
        ogAlbum = "Original Album",
        ogTitle = "Original Title",
        pos = 1,
        title = "Save Your Tears",
        track = 1,
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
            onClickUpNextTrackItem = {},
            onClickQueueItem = { index: Int -> },
        )
    }
}
