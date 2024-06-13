package com.android.swingmusic.presentation.activity

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.android.swingmusic.artist.presentation.screen.ArtistsScreen
import com.android.swingmusic.folder.presentation.screen.FoldersAndTracksScreen
import com.android.swingmusic.player.presentation.compose.MiniPlayer
import com.android.swingmusic.player.presentation.compose.NowPlayingScreen
import com.android.swingmusic.player.presentation.compose.UpNextQueueScreen
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.service.MediaSessionManager
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
        Timber.e("onStart: intents: ${intent.extras?.size()}") // TODO: Check this

        if (
            mediaControllerViewModel.getMediaController() == null ||
            (this::controllerFuture.isInitialized).not()
        ) {
            val sessionToken = MediaSessionManager.sessionToken
            if (sessionToken != null) {
                // Use the existing session token to build the MediaController
                controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
                controllerFuture.addListener(
                    {
                        val mediaController = controllerFuture.get()
                        mediaControllerViewModel.reconnectMediaController(mediaController)
                    }, MoreExecutors.directExecutor()
                )

            } else {
                // Create a new session if no existing token is found
                val newSessionToken =
                    SessionToken(this, ComponentName(this, PlaybackService::class.java))
                controllerFuture = MediaController.Builder(this, newSessionToken).buildAsync()
                controllerFuture.addListener(
                    {
                        val mediaController = controllerFuture.get()
                        mediaControllerViewModel.setMediaController(mediaController)
                    }, MoreExecutors.directExecutor()
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SwingMusicTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                      MiniPlayer()
                    }
                ) {
                    /** TODO: Add a bottom nav bar,
                     *        A mini player - visible across the entire app -> Except when excepted
                     * */

                    Surface(modifier = Modifier.padding(it)) {
                        //For testing purposes ONLY
                         // FoldersAndTracksScreen()
                        // ArtistsScreen()
                         // NowPlayingScreen()
                         UpNextQueueScreen()

                        // @Reserved("Custom")
                        // G_UpNextQueueScreen()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::controllerFuture.isInitialized) {
            mediaControllerViewModel.releaseMediaController(controllerFuture)
        }
    }
}
