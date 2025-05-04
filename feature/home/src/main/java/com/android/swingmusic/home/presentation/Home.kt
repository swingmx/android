package com.android.swingmusic.home.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun Home(
    mediaControllerViewModel: MediaControllerViewModel
) {
    SideEffect {
        mediaControllerViewModel.refreshBaseUrl()
    }

    SwingMusicTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = "Welcome Home")
        }
    }
}
