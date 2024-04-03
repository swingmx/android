package com.android.swingmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.swingmusic.core.util.PlayerState
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.uicomponent.presentation.component.PlayingTrackIndicator
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val foldersViewModel: FoldersViewModel by viewModels<FoldersViewModel>()

        setContent {
            SwingMusicTheme {
                DisposableEffect(key1 = true) {
                    foldersViewModel.getFoldersAndTracks()
                    onDispose { }
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.wrapContentSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    PlayingTrackIndicator(playerState = PlayerState.PLAYING)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SwingMusicTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Greeting("Swing")
        }
    }
}