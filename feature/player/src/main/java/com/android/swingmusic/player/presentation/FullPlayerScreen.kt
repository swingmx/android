package com.android.swingmusic.player.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlayerState
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import java.util.Locale

@Composable
fun FullPlayerScreen(
    track: Track,
    progress: Float = 0F,
    playerState: PlayerState,
    repeatMode: RepeatMode,
    shuffleMode: ShuffleMode,
    onClickArtist: (artistHash: String) -> Unit,
    onToggleRepeatMode: (RepeatMode) -> Unit,
    onClickPrev: () -> Unit,
    onTogglePlayerState: (PlayerState) -> Unit,
    onClickNext: () -> Unit,
    onToggleShuffleMode: (ShuffleMode) -> Unit,
    onSliderPositionChanged: (Float) -> Unit,
    onClickMore: () -> Unit,
    onClickLyrics: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onClickQueue: () -> Unit
) {
    val artistsSeparator by remember {
        derivedStateOf { if (track.trackArtists.size == 2) " & " else ", " }
    }

    val fileType by remember {
        derivedStateOf {
            track.filepath.substringAfterLast(".").uppercase(Locale.ROOT)
        }
    }

    val repeatModeIcon by remember {
        derivedStateOf {
            when (repeatMode) {
                RepeatMode.REPEAT_ONE -> R.drawable.repeat_one
                else -> R.drawable.repeat_all
            }
        }
    }

    val playerStateIcon by remember {
        derivedStateOf {
            when (playerState) {
                PlayerState.PLAYING -> R.drawable.pause_circle
                PlayerState.PAUSED -> R.drawable.play_circle
                else -> R.drawable.disabled
            }
        }
    }

    SwingMusicTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Track Image
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(7)),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("$BASE_URL/img/t/${track.image}")
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.img_2),
                        fallback = painterResource(R.drawable.img_2),
                        error = painterResource(R.drawable.img_2),
                        contentDescription = "Track Image",
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(.78F)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(modifier = Modifier.fillMaxWidth()) {
                                track.trackArtists.forEachIndexed { index, trackArtist ->
                                    item {
                                        // TODO: Hide tap indicator
                                        Text(
                                            modifier = Modifier.clickable {
                                                onClickArtist(trackArtist.artistHash)
                                            },
                                            text = trackArtist.name,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (index != track.trackArtists.lastIndex) {
                                            Text(
                                                text = artistsSeparator,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = .84F
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.inverseOnSurface
                                ),
                            onClick = {
                                onToggleFavorite(track.isFavorite)
                            }) {
                            val icon =
                                if (track.isFavorite) R.drawable.fav_filled
                                else R.drawable.fav_not_filled
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = "Favorite"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column {
                        // TODO: Figure out how to update progress in sync with duration
                        Slider(
                            modifier = Modifier.fillMaxWidth(),
                            value = progress,
                            onValueChange = { position ->
                                onSliderPositionChanged(position)

                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            // TODO: Figure out how to calculate these durations
                            Text(
                                text = "01:23",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                            )
                            Text(
                                text = "02:59",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.inverseOnSurface
                                ), onClick = {
                                onClickPrev()
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.prev),
                                contentDescription = "Prev"
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(82.dp),
                            onClick = {
                                onTogglePlayerState(playerState)
                            }) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                painter = painterResource(id = playerStateIcon),
                                contentDescription = "Play/Pause"
                            )
                        }

                        IconButton(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.inverseOnSurface
                                ), onClick = {
                                onClickNext()
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.next),
                                contentDescription = "Next"
                            )
                        }
                    }
                }

                // Bitrate, Track format
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .wrapContentSize()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fileType,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = track.bitrate.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .padding(vertical = 12.dp, horizontal = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        onClickLyrics()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.lyrics_delete_this),
                            contentDescription = "Lyrics"
                        )
                    }

                    IconButton(onClick = {
                        onToggleRepeatMode(repeatMode)
                    }) {
                        Icon(
                            painter = painterResource(id = repeatModeIcon),
                            tint = if (repeatMode == RepeatMode.REPEAT_NONE)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = .3F)
                            else MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Repeat"
                        )
                    }

                    IconButton(onClick = {
                        onClickQueue()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.queue),
                            contentDescription = "Queue"
                        )
                    }

                    IconButton(onClick = {
                        onToggleShuffleMode(shuffleMode)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.shuffle),
                            tint = if (shuffleMode == ShuffleMode.SHUFFLE_OFF)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = .3F)
                            else MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Shuffle"
                        )
                    }

                    IconButton(onClick = {
                        onClickMore()
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More"
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    device = Devices.PIXEL_5
)
@Composable
fun FullPlayerScreenPreview() {
    val lilPeep = TrackArtist(
        artistHash = "lilpeep123",
        image = "lilpeep.jpg",
        name = "Lil Peep"
    )

    val juice = TrackArtist(
        artistHash = "juice123",
        image = "juice.jpg",
        name = "Juice WRLD"
    )
    val young = TrackArtist(
        artistHash = "juice123",
        image = "juice.jpg",
        name = "Young Thug"
    )
    val weeknd = TrackArtist(
        artistHash = "juice123",
        image = "juice.jpg",
        name = "The Weeknd"
    )

    val albumArtists = listOf(lilPeep, juice)
    val artists = listOf(juice, young)
    val genre = listOf("Rap", "Emo")

    val track = Track(
        album = "Sample Album",
        albumTrackArtists = albumArtists,
        albumHash = "albumHash123",
        artistHashes = "artistHashes123",
        trackArtists = listOf(weeknd),
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

    SwingMusicTheme {
        FullPlayerScreen(
            track = track,
            progress = .22F,
            playerState = PlayerState.PLAYING,
            repeatMode = RepeatMode.REPEAT_NONE,
            shuffleMode = ShuffleMode.SHUFFLE_OFF,
            onClickArtist = {},
            onToggleRepeatMode = {},
            onClickPrev = {},
            onTogglePlayerState = {},
            onClickNext = {},
            onToggleShuffleMode = {},
            onSliderPositionChanged = {},
            onClickLyrics = {},
            onToggleFavorite = {},
            onClickQueue = {},
            onClickMore = {}
        )
    }
}
