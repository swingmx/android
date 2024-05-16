package com.android.swingmusic.uicomponent.presentation.component

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.util.PlayerState
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
fun MiniPlayer(
    trackHash: String,
    trackTitle: String,
    trackImage: String,
    playerState: PlayerState,
    progress: Float,
    onClickPlayerItem: (hash: String) -> Unit,
    onClickPlayerIcon: (state: PlayerState) -> Unit,
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(16)),
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
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
                        )
                    }

                    // Player Indicator
                    IconButton(
                        modifier = Modifier.padding(end = 4.dp),
                        onClick = {
                            onClickPlayerIcon(playerState)
                        }) {
                        when (playerState) {
                            PlayerState.PLAYING -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.pause_icon),
                                    contentDescription = "playing State indicator"
                                )
                            }

                            PlayerState.PAUSED -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.play_arrow),
                                    contentDescription = "paused State indicator"
                                )
                            }

                            PlayerState.UNSPECIFIED -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.disabled),
                                    tint = MaterialTheme.colorScheme.inverseSurface.copy(alpha = .5F),
                                    contentDescription = "paused State indicator"
                                )
                            }
                        }
                    }
                }

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    progress = progress,
                    strokeCap = StrokeCap.Square
                )
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.NEXUS_5, showBackground = true)
@Composable
fun MiniPlayerPreview() {
    SwingMusicTheme {
        MiniPlayer(
            playerState = PlayerState.PAUSED,
            trackHash = "abc123",
            trackTitle = "Track title is too large to be displayed",
            trackImage = "https://image",
            progress = 0.2F,
            onClickPlayerItem = {

            },
            onClickPlayerIcon = {

            }
        )
    }
}
