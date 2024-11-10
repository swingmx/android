package com.android.swingmusic.player.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import kotlin.math.roundToInt

@Composable
private fun MiniPlayer(
    trackTitle: String,
    trackImage: String,
    playbackState: PlaybackState,
    isBuffering: Boolean,
    playbackProgress: Float, // also called seekPosition
    onClickPlayerItem: () -> Unit,
    onTogglePlaybackState: () -> Unit,
    onResumePlayBackFromError: () -> Unit,
    onSwipeLeft: () -> Unit, //  <<- |
    onSwipeRight: () -> Unit, // | ->>
    baseUrl: String,
) {
    var swipeDistance by remember { mutableFloatStateOf(0F) }
    val interactions = remember { MutableInteractionSource() }

    Surface {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (swipeDistance > 50) {
                                    onSwipeRight()
                                } else if (swipeDistance < -50) {
                                    onSwipeLeft()
                                }
                                swipeDistance = 0F
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            swipeDistance += dragAmount
                        }
                    }
                    .clickable(
                        interactionSource = interactions,
                        indication = null
                    ) {
                        onClickPlayerItem()
                    }
            ) {
                // Image, Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.8F)
                        .padding(8.dp)
                        // div/3 the offset to kinda slow down the displacement
                        .offset { IntOffset(swipeDistance.roundToInt() / 3, y = 0) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(18)),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("${baseUrl}img/thumbnail/small/${trackImage}")
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
                        color = if ((swipeDistance.toInt() != 0))
                            MaterialTheme.colorScheme.onSurface.copy(alpha = .25F) else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                    )
                }

                // Player State Indicator
                IconButton(
                    modifier = Modifier
                        .padding(end = 8.dp),
                    onClick = {
                        if (playbackState == PlaybackState.ERROR) {
                            onResumePlayBackFromError()
                        } else {
                            onTogglePlaybackState()
                        }
                    }
                ) {
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
                        // PAUSED, ERROR
                        else -> {
                            Icon(
                                painter = painterResource(id = R.drawable.play_arrow),
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
                progress = { playbackProgress },
                strokeCap = StrokeCap.Square
            )
        }
    }
}

/**
 * A public overload that implements [MiniPlayer] under the hood.
 * It couples its MiniPlayer to [MediaControllerViewModel]
 * */

@Composable
fun MiniPlayer(
    mediaControllerViewModel: MediaControllerViewModel,
    onClickPlayerItem: () -> Unit
) {
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    playerUiState.nowPlayingTrack?.let { track ->
        MiniPlayer(
            trackTitle = track.title,
            trackImage = track.image,
            playbackState = playerUiState.playbackState,
            isBuffering = playerUiState.isBuffering,
            playbackProgress = playerUiState.seekPosition,
            baseUrl = baseUrl ?: "",
            onClickPlayerItem = {
                onClickPlayerItem()
            },
            onTogglePlaybackState = {
                mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnTogglePlayerState)
            },
            onSwipeLeft = {
                mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnNext)
            },
            onSwipeRight = {
                mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnPrev)
            },
            onResumePlayBackFromError = {
                mediaControllerViewModel.onPlayerUiEvent(PlayerUiEvent.OnResumePlaybackFromError)
            }
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
fun MiniPlayerPreview() {
    SwingMusicTheme_Preview {
        MiniPlayer(
            baseUrl = "",
            trackTitle = "Track title is too large to be displayed",
            trackImage = "https://image",
            playbackState = PlaybackState.PLAYING,
            isBuffering = true,
            playbackProgress = 0.2F,
            onClickPlayerItem = {},
            onTogglePlaybackState = {},
            onResumePlayBackFromError = {},
            onSwipeLeft = {},
            onSwipeRight = {}
        )
    }
}
