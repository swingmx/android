package com.android.swingmusic.presentation.activity

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.android.swingmusic.presentation.compose.FullScreenPlayerScreen
import com.android.swingmusic.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.service.PlaybackService
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mediaControllerViewModel: MediaControllerViewModel by viewModels<MediaControllerViewModel>()
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    override fun onStart() {
        super.onStart()
        if (mediaControllerViewModel.getMediaController() == null) {
            val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
            controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener({
                val mediaController = controllerFuture.get()
                mediaControllerViewModel.setMediaController(mediaController)

                // TODO: Create UI events that calls this for the current queue and track
                mediaControllerViewModel.loadMediaItems()
            }, MoreExecutors.directExecutor())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SwingMusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    /** TODO: Add a bottom nav bar,
                     *        A mini player - visible across the entire app -> Except when excepted
                     * */
                    // For testing purposes ONLY
                    // FoldersAndTracksScreen()
                    // ArtistsScreen()
                    FullScreenPlayerScreen()
                    // UpNextQueueScreen()

                    /*Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        MiniPlayer()

                       Spacer(modifier = Modifier.height(64.dp))
                    }*/
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaControllerViewModel.releaseMediaController(controllerFuture)

        Timber.i("-------------------  Media Session Disconnected  ---------------------")
        Timber.i("-------------------------  App Destroyed  ----------------------------")
    }
}
