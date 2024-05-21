package com.android.swingmusic.player.presentation

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlayerState
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingScreen(
    nowPlayingTrackIndex: Int,
    playerState: PlayerState,
    queue: List<Track>,
) {
    // TODO(level: LOW) -> Move the up-next logic to a viewModel and pass nextTrack and queue as states
    val nextTrackIndex = when {
        nowPlayingTrackIndex == queue.lastIndex -> 0
        nowPlayingTrackIndex > queue.lastIndex -> 0
        else -> nowPlayingTrackIndex + 1
    }

    val upNextTrack by remember {
        derivedStateOf {
            queue[nextTrackIndex]
        }
    }

    SwingMusicTheme {
        Scaffold(
            topBar = {
                Text(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 8.dp
                    ),
                    text = "Up Next",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                if (queue.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(bottom = 54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No queued tracks found!")
                        }
                    }

                    return@LazyColumn
                }
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.inverseOnSurface)
                            .padding(
                                start = 4.dp,
                                end = 12.dp,
                                top = 12.dp,
                                bottom = 12.dp
                            ),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp),
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

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .wrapContentSize()
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(vertical = 3.dp, horizontal = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (nextTrackIndex + 1).toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                itemsIndexed(queue) { index, item ->
                    TrackItem(
                        track = item,
                        playerState = playerState,
                        isCurrentTrack = index == nowPlayingTrackIndex,
                        trackQueueNumber = index + 1,
                        onClickTrackItem = {},
                        onClickMoreVert = {}
                    )
                }
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun NowPlayingPreview() {
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

    val queue = listOf(
        track,
        track.copy(title = "Popular", trackHash = "popular"),
        track.copy(title = "One Right Now", trackHash = "one")
    )

    SwingMusicTheme {
        NowPlayingScreen(
            nowPlayingTrackIndex = 2,
            playerState = PlayerState.PLAYING,
            queue = queue
        )
    }
}
