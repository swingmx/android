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
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.android.swingmusic.R
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject
    lateinit var authRepository: AuthRepository
    private var mediaSession: MediaSession? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelName(R.string.media3_notification_channel_name)
            .build()
        setMediaNotificationProvider(notificationProvider)

        serviceScope.launch {
            val accessToken = authRepository.getAccessToken()
                ?: "TAG: $this SERVICE -> TOKEN NOT FOUND"

            val loadControlBuilder = DefaultLoadControl.Builder().apply {
                setBufferDurationsMs(
                    DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                ).setBackBuffer(30_000, false)
            }

            val dataSourceFactory = CustomDataSourceFactory(this@PlaybackService, accessToken)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)

            val player = ExoPlayer.Builder(this@PlaybackService)
                .setMediaSourceFactory(mediaSource)
                .setLoadControl(loadControlBuilder.build())
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setDeviceVolumeControlEnabled(true)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build()

            val intent = packageManager.getLaunchIntentForPackage(packageName)
            val pendingIntent = PendingIntent.getActivity(
                this@PlaybackService, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
            )

            mediaSession = MediaSession.Builder(this@PlaybackService, player)
                .setSessionActivity(pendingIntent)
                .build()

            SessionTokenManager.sessionToken = mediaSession?.token
        }
    }

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

        serviceScope.cancel()

        super.onDestroy()
    }
}
