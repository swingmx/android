package com.android.swingmusic.player.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme

@Composable
fun NowPlayingScreen(
    nowPlayingTrackIndex: Int = 0,
    queue: List<Track> = emptyList(),
) {
    SwingMusicTheme {
        Scaffold(
            topBar = {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Now Playing",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        ){ paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(queue) {

                }
            }
        }
    }
}

@Preview
@Composable
fun NowPlayingPreview() {
    SwingMusicTheme {
        NowPlayingScreen()
    }
}