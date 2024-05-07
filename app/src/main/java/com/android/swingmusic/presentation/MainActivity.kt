package com.android.swingmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.swingmusic.artist.presentation.screen.ArtistsScreen
import com.android.swingmusic.artist.presentation.viewmodel.ArtistsViewModel
import com.android.swingmusic.folder.presentation.screen.FoldersAndTracksScreen
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val foldersViewModel: FoldersViewModel by viewModels<FoldersViewModel>()
    private val artistsViewModel: ArtistsViewModel by viewModels<ArtistsViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SwingMusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    // For interaction purposes
                    // FoldersAndTracksScreen(foldersViewModel)
                    ArtistsScreen(artistsViewModel)
                }
            }
        }
    }
}
