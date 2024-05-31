package com.android.swingmusic.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.presentation.event.PlayerUiEvent
import com.android.swingmusic.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
private fun MiniPlayer(
    trackHash: String,
    trackTitle: String,
    trackImage: String,
    playbackState: PlaybackState,
    isBuffering: Boolean,
    playbackProgress: Float, // also called seekPosition
    onClickPlayerItem: (hash: String) -> Unit,
    onTogglePlaybackState: () -> Unit,
) {
    SwingMusicTheme {
        Surface {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
                    .clickable {
                        onClickPlayerItem(trackHash)
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image, Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.8F)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18)),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("$BASE_URL/img/t/s/${trackImage}")
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.audio_fallback),
                            fallback = painterResource(R.drawable.audio_fallback),
                            error = painterResource(R.drawable.audio_fallback),
                            contentDescription = "Track Image",
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = trackTitle,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                        )
                    }

                    // Player State Indicator
                    IconButton(
                        modifier = Modifier
                            .padding(end = 8.dp),
                        onClick = {
                            if (playbackState != PlaybackState.ERROR)
                                onTogglePlaybackState()
                        }) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = (.75).dp,
                                strokeCap = StrokeCap.Round
                            )
                        }
                        when (playbackState) {
                            PlaybackState.PLAYING -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.pause_icon),
                                    contentDescription = "playing state indicator"
                                )
                            }

                            PlaybackState.PAUSED -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.play_arrow),
                                    contentDescription = "paused state indicator"
                                )
                            }

                            PlaybackState.ERROR -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.error),
                                    tint = MaterialTheme.colorScheme.inverseSurface.copy(alpha = .5F),
                                    contentDescription = "paused state indicator"
                                )
                            }
                        }
                    }
                }

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    progress = playbackProgress,
                    strokeCap = StrokeCap.Square
                )
            }
        }
    }
}

/**
 * A public overload that implements [MiniPlayer] under the hood.
 * It couples its MiniPlayer to [MediaControllerViewModel]
 * */

@Composable
fun MiniPlayer(mediaControllerViewModel: MediaControllerViewModel = viewModel()) {
    val playerUiState by remember { mediaControllerViewModel.playerUiState }
    MiniPlayer(
        trackHash = playerUiState.track.trackHash,
        trackTitle = playerUiState.track.title,
        trackImage = playerUiState.track.image,
        playbackState = playerUiState.playbackState,
        isBuffering = playerUiState.isBuffering,
        playbackProgress = playerUiState.seekPosition,
        onClickPlayerItem = {
            // TODO: Open FullScreen player in a full screen bottom sheet
        },
        onTogglePlaybackState = {
            mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnTogglePlayerState)
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.NEXUS_5, showBackground = true)
@Composable
fun MiniPlayerPreview() {
    SwingMusicTheme {
        MiniPlayer(
            playbackState = PlaybackState.PLAYING,
            isBuffering = true,
            trackHash = "abc123",
            trackTitle = "Track title is too large to be displayed",
            trackImage = "https://image",
            playbackProgress = 0.2F,
            onClickPlayerItem = {},
            onTogglePlaybackState = {}
        )
    }
}
