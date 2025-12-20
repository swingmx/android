package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
fun PlayingTrackIndicator(
    playbackState: PlaybackState
) {
    // Background
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                if (playbackState == PlaybackState.ERROR)
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = .75F) else
                    MaterialTheme.colorScheme.surface.copy(alpha = .750F)
            )
    )

    // Sound Signal Bars
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (playbackState) {
            PlaybackState.PLAYING -> {
                SoundSignalBars(animate = true)
            }

            PlaybackState.PAUSED -> {
                SoundSignalBars(animate = false)
            }

            PlaybackState.ERROR -> {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.error),
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = "Error Icon"
                )
            }
        }
    }
}


@Preview
@Composable
fun PlayingTrackIndicatorPreview() {
    SwingMusicTheme {
        Box(
            modifier = Modifier.size(48.dp)
        ) {
            PlayingTrackIndicator(playbackState = PlaybackState.ERROR)
        }
    }
}
