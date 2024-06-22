package com.android.swingmusic.service

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject
    lateinit var authRepository: AuthRepository
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val loadControlBuilder = DefaultLoadControl.Builder()
        loadControlBuilder.setBufferDurationsMs(
            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
            DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        ).setBackBuffer(30_000, false) // 30 sec cache
        val loadControl: LoadControl = loadControlBuilder.build()

        val accessToken = authRepository.getAccessToken() ?: "TAG: $this SERVICE -> TOKEN NOT FOUND"
        val dataSourceFactory = CustomDataSourceFactory(this, accessToken)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)

        // val mediaSource = DefaultMediaSourceFactory(dataSourceFactory)
        // val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
        // val mediaSource = DashMediaSource.Factory(dataSourceFactory)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSource)
            .setLoadControl(loadControl)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setDeviceVolumeControlEnabled(true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.putExtra("DESTINATION", "NOW_PLAYING_SCREEN")
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        // Store the session token
        SessionTokenManager.sessionToken = mediaSession?.token
    }

    // TODO: Save Played Track on Transition within this Service when app is not running but service is.

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED
        ) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            this.release()
            mediaSession = null
        }
        // Clear Tokens when they're not needed anymore
        SessionTokenManager.sessionToken = null
        AuthTokenHolder.accessToken = null
        AuthTokenHolder.refreshToken = null

        super.onDestroy()
    }
}
