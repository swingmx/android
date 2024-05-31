package com.android.swingmusic.presentation.compose

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers.RED_DOMINATED_EXAMPLE
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.presentation.event.PlayerUiEvent
import com.android.swingmusic.presentation.state.PlayerUiState
import com.android.swingmusic.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.galaxygoldfish.waveslider.PillThumb
import com.galaxygoldfish.waveslider.WaveSlider
import com.galaxygoldfish.waveslider.WaveSliderDefaults
import java.util.Locale

@Composable
private fun FullScreenPlayer(
    track: Track?,
    seekPosition: Float = 0F,
    playbackDuration: String,
    trackDuration: String,
    playbackState: PlaybackState,
    isBuffering: Boolean,
    repeatMode: RepeatMode,
    shuffleMode: ShuffleMode,
    onClickArtist: (artistHash: String) -> Unit,
    onToggleRepeatMode: (RepeatMode) -> Unit,
    onClickPrev: () -> Unit,
    onTogglePlayerState: (PlaybackState) -> Unit,
    onClickNext: () -> Unit,
    onToggleShuffleMode: (ShuffleMode) -> Unit,
    onSeekPlayBack: (Float) -> Unit,
    onClickMore: () -> Unit,
    onClickLyricsIcon: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onClickQueue: () -> Unit
) {
    if (track == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Loading track...")
        }

        return
    }

    val artistsSeparator by remember {
        derivedStateOf { if (track.trackArtists.size == 2) " & " else ", " }
    }
    val fileType by remember {
        derivedStateOf {
            track.filepath.substringAfterLast(".").uppercase(Locale.ROOT)
        }
    }
    val repeatModeIcon = when (repeatMode) {
        RepeatMode.REPEAT_ONE -> R.drawable.repeat_one
        else -> R.drawable.repeat_all
    }
    val playbackStateIcon = when (playbackState) {
        PlaybackState.PLAYING -> R.drawable.pause_icon // TODO: Replace with a thinner icon
        PlaybackState.PAUSED -> R.drawable.play_arrow
        PlaybackState.ERROR -> R.drawable.error
    }

    SwingMusicTheme {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
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
                        placeholder = painterResource(R.drawable.audio_fallback),
                        fallback = painterResource(R.drawable.audio_fallback),
                        error = painterResource(R.drawable.audio_fallback),
                        contentDescription = "Track Image",
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(28.dp))

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
                                        Text(
                                            modifier = Modifier
                                                .clickable(
                                                    onClick = { onClickArtist(trackArtist.artistHash) },
                                                    indication = null,  // Disables the ripple effect
                                                    interactionSource = remember { MutableInteractionSource() }
                                                ),
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

                    Spacer(modifier = Modifier.height(28.dp))

                    Column {
                        // TODO: Figure out how to update progress in sync with duration

                        WaveSlider(
                            modifier = Modifier.height(12.dp),
                            value = seekPosition,
                            onValueChange = { value ->
                                onSeekPlayBack(value)
                            },
                            animationOptions = WaveSliderDefaults.animationOptions(
                                reverseDirection = false,
                                flatlineOnDrag = true,
                                animateWave = playbackState == PlaybackState.PLAYING,
                                reverseFlatline = false
                            ),
                            colors = WaveSliderDefaults.colors(
                                inactiveTrackColor = MaterialTheme.colorScheme.inverseOnSurface
                            ),
                            thumb = { PillThumb() },
                            waveOptions = WaveSliderDefaults.waveOptions(
                                amplitude = 12F,
                                frequency = 0.07F
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = playbackDuration,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                            )
                            Text(
                                text = trackDuration,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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

                        Box(
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    onTogglePlayerState(playbackState)
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier.wrapContentSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                //TODO Use colored box here
                                if (playbackState == PlaybackState.ERROR) {
                                    Icon(
                                        modifier = Modifier
                                            .padding(horizontal = 5.dp)
                                            .size(70.dp),
                                        painter = painterResource(id = playbackStateIcon),
                                        tint = if (isBuffering)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = .25F) else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = .5F),
                                        contentDescription = "Error state"
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(70.dp)
                                            .width(80.dp)
                                            .clip(RoundedCornerShape(32))
                                            .background(MaterialTheme.colorScheme.onSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(44.dp),
                                            tint = MaterialTheme.colorScheme.surface,
                                            painter = painterResource(id = playbackStateIcon),
                                            contentDescription = "Play/Pause"
                                        )
                                    }
                                }

                                if (isBuffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(50.dp),
                                        strokeCap = StrokeCap.Round,
                                        strokeWidth = 1.dp,
                                        color = if (playbackState == PlaybackState.ERROR)
                                            MaterialTheme.colorScheme.onSurface else
                                            MaterialTheme.colorScheme.surface.copy(alpha = .75F)
                                    )
                                }
                            }
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
                        onClickLyricsIcon()
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
                            tint = if (repeatMode == RepeatMode.REPEAT_OFF)
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

/**
 * Expose a public Composable tied to MediaControllerViewModel
 * **/
@Composable
fun FullScreenPlayerScreen(
    mediaControllerViewModel: MediaControllerViewModel = viewModel()
) {
    val playerUiState: PlayerUiState by mediaControllerViewModel.playerUiState

    FullScreenPlayer(
        track = playerUiState.track,
        seekPosition = playerUiState.seekPosition,
        playbackDuration = playerUiState.playbackDuration,
        trackDuration = playerUiState.trackDuration,
        playbackState = playerUiState.playbackState,
        repeatMode = playerUiState.repeatMode,
        shuffleMode = playerUiState.shuffleMode,
        isBuffering = playerUiState.isBuffering,
        onClickArtist = {},
        onToggleRepeatMode = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnToggleRepeatMode
            )
        },
        onClickPrev = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnPrev
            )
        },
        onTogglePlayerState = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnTogglePlayerState
            )
        },
        onClickNext = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnNext
            )
        },
        onToggleShuffleMode = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnToggleShuffleMode
            )
        },
        onSeekPlayBack = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnSeekPlayBack(it)
            )
        },
        onClickLyricsIcon = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnClickLyricsIcon
            )
        },
        onToggleFavorite = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnToggleFavorite
            )
        },
        onClickQueue = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnClickQueue
            )
        },
        onClickMore = {
            mediaControllerViewModel.onPlayerUiEvent(
                PlayerUiEvent.OnClickMore
            )
        }
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = RED_DOMINATED_EXAMPLE,
    device = Devices.PIXEL_5
)
@Composable
fun FullPlayerPreview() {
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
        FullScreenPlayer(
            track = track,
            seekPosition = .22F,
            playbackDuration = "01:23",
            trackDuration = "02:59",
            playbackState = PlaybackState.PLAYING,
            isBuffering = true,
            repeatMode = RepeatMode.REPEAT_OFF,
            shuffleMode = ShuffleMode.SHUFFLE_OFF,
            onClickArtist = {},
            onToggleRepeatMode = {},
            onClickPrev = {},
            onTogglePlayerState = {},
            onClickNext = {},
            onToggleShuffleMode = {},
            onSeekPlayBack = {},
            onClickLyricsIcon = {},
            onToggleFavorite = {},
            onClickQueue = {},
            onClickMore = {}
        )
    }
}
