package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.domain.util.PlayerState
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.CoilGifLoader

@Composable
fun PlayingTrackIndicator(
    playerState: PlayerState
) {
    SwingMusicTheme {
        // Background
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = .50F))
                .size(48.dp)
        )

        // Gif/Image
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (playerState) {
                PlayerState.PLAYING -> {
                    CoilGifLoader(
                        resource = R.raw.playing_anim,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                PlayerState.PAUSED -> {
                    Image(
                        painter = painterResource(id = R.drawable.paused),
                        contentDescription = "Paused State Indicator"
                    )
                }

                PlayerState.UNSPECIFIED -> {

                }
            }
        }
    }
}
