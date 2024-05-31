package com.android.swingmusic.presentation.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.presentation.event.PlayerUiEvent
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnClickLyricsIcon
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnClickMore
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnClickQueue
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnNext
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnPlayBackComplete
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnPrev
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnSeekPlayBack
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnToggleFavorite
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnTogglePlayerState
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnToggleRepeatMode
import com.android.swingmusic.presentation.event.PlayerUiEvent.OnToggleShuffleMode
import com.android.swingmusic.presentation.state.PlayerUiState
import com.android.swingmusic.presentation.util.formatDuration
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

class MediaControllerViewModel : ViewModel() {
    private val playerListener = PlayerListener()
    private var mediaController: MediaController? = null

    fun setMediaController(controller: MediaController) {
        if (mediaController == null)
            mediaController = controller
    }

    fun getMediaController() = mediaController

    // TODO: Handle Queue, playingTrack, playingTrackIndex

    val track = Track(
        album = "Sample Album",
        albumTrackArtists = listOf(),
        albumHash = "albumHash123",
        artistHashes = "artistHashes123",
        trackArtists = listOf(
            TrackArtist(
                artistHash = "aefcb0afd5",
                image = "aefcb0afd5.webp",
                name = "Weeknd"
            )
        ),
        ati = "ati123",
        bitrate = 320,
        copyright = "Copyright © 2024",
        createdDate = 1648731600.0, // Sample timestamp
        date = 2024,
        disc = 1,
        duration = 216, // duration in seconds
        filepath = "/home/eric/Swing/Iann Mix/The Weeknd - Save Your Tears.mp3",
        folder = "/home/eric/Swing/Iann Mix",
        genre = listOf("pop"),
        image = "aefcb0afd5.webp",
        isFavorite = false,
        lastMod = 1648731600, // Sample timestamp
        ogAlbum = "",
        ogTitle = "",
        pos = 1,
        title = "Save Your Tears",
        track = 1,
        trackHash = "bbdb9302c2"
    )

    val playerUiState: MutableState<PlayerUiState> = mutableStateOf(
        PlayerUiState(track = track)
    )

    init {
        val trackDuration = track.duration.formatDuration()

        playerUiState.value = playerUiState.value.copy(
            trackDuration = trackDuration
        )

        viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    if (controller.playbackState == Player.STATE_READY) {
                        val exoPlayerPosition = controller.currentPosition
                        val seekPosition = (exoPlayerPosition / 1000F)
                            .div(track.duration)
                            .coerceIn(0F, 1F)
                        val playbackDuration = (exoPlayerPosition / 1000F).roundToInt()

                        playerUiState.value = playerUiState.value.copy(
                            seekPosition = seekPosition,
                            playbackDuration = playbackDuration.formatDuration()
                        )
                    }
                }

                delay(1000L)
            }
        }
    }

    public fun loadMediaItems() {
        _loadMediaItems(listOf(track, track))
    }

    private fun _loadMediaItems(tracks: List<Track>) {
        viewModelScope.launch {

            val mediaItems = tracks.map { track ->
                val path = "$BASE_URL${""}file/${track.trackHash}?filepath=${track.filepath}"
                val encodedPath = Uri.encode(path, "/:;?&=+$,@!*()^.<>_-")
                val uri = Uri.parse(encodedPath)

                val artworkUri = Uri.parse("$BASE_URL${""}img/t/${track.image}")
                val artists = track.trackArtists.joinToString(", ") { it.name }

                MediaItem.Builder()
                    .setUri(uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(artists)
                            .setTitle(track.title)
                            .setArtworkUri(artworkUri)
                            .setGenre(track.genre.joinToString(", ") { it })
                            .build()
                    )
                    .build()
            }

            mediaController?.apply {
                clearMediaItems()
                addMediaItems(mediaItems)
                prepare()
                setPlaybackSpeed(1F)
                addListener(playerListener)
                playWhenReady = true

                playerUiState.value = playerUiState.value.copy(
                    playbackState = PlaybackState.PLAYING
                )
            }
        }
    }

    fun onPlayerUiEvent(event: PlayerUiEvent) {
        mediaController?.let { controller ->
            when (event) {
                is OnSeekPlayBack -> {
                    val toValue = event.value // Ranges from 0.0 to 1.0
                    val playbackDuration = (toValue * track.duration).toInt()

                    playerUiState.value = playerUiState.value.copy(
                        seekPosition = event.value,
                        playbackDuration = playbackDuration.formatDuration()
                    )
                    val trackDurationMs = playerUiState.value.track.duration * 1000
                    val positionInMs = (trackDurationMs * toValue).toLong()
                    val seekPosition = positionInMs.coerceIn(0, trackDurationMs.toLong())

                    if (controller.playbackState == Player.STATE_READY) {
                        controller.seekTo(seekPosition)
                    } else {
                        mediaController?.apply {
                            prepare()
                            seekTo(seekPosition)
                            pause()
                        }
                    }
                }

                is OnPrev -> {
                    controller.seekToPrevious()
                }

                is OnNext -> {
                    controller.seekToNext()
                }

                is OnTogglePlayerState -> {
                    val playerState = playerUiState.value.playbackState

                    playerUiState.value = playerUiState.value.copy(
                        playbackState = when (playerState) {
                            PlaybackState.PLAYING -> {
                                controller.pause()
                                PlaybackState.PAUSED
                            }

                            PlaybackState.PAUSED -> {
                                controller.setPlaybackSpeed(1F)
                                controller.play()
                                PlaybackState.PLAYING
                            }

                            else -> playerState
                        }
                    )
                    when (controller.playbackState) {
                        Player.STATE_ENDED -> {
                            controller.prepare()
                            controller.seekTo(0)
                            controller.play()
                        }

                        else -> {

                        }
                    }
                }

                is OnPlayBackComplete -> {}

                is OnToggleFavorite -> {}

                is OnClickLyricsIcon -> {}

                is OnToggleRepeatMode -> {
                    val repeatMode = playerUiState.value.repeatMode

                    playerUiState.value = playerUiState.value.copy(
                        repeatMode = when (repeatMode) {
                            RepeatMode.REPEAT_OFF -> {
                                // Set exoPlayer repeat mode
                                controller.repeatMode = Player.REPEAT_MODE_ALL
                                RepeatMode.REPEAT_ALL
                            }

                            RepeatMode.REPEAT_ALL -> {
                                controller.repeatMode = Player.REPEAT_MODE_ONE
                                RepeatMode.REPEAT_ONE
                            }

                            RepeatMode.REPEAT_ONE -> {
                                controller.repeatMode = Player.REPEAT_MODE_OFF
                                RepeatMode.REPEAT_OFF
                            }
                        }
                    )
                }

                is OnToggleShuffleMode -> {
                    val shuffleMode = playerUiState.value.shuffleMode

                    playerUiState.value = playerUiState.value.copy(
                        shuffleMode = if (shuffleMode == ShuffleMode.SHUFFLE_ON)
                            ShuffleMode.SHUFFLE_OFF else ShuffleMode.SHUFFLE_ON
                    )
                }

                is OnClickQueue -> {}

                is OnClickMore -> {}

                else -> {}
            }
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {}

                Player.STATE_BUFFERING -> {
                    playerUiState.value = playerUiState.value.copy(
                        isBuffering = true
                    )
                }

                Player.STATE_READY -> {
                    if (mediaController?.isPlaying == true) {
                        playerUiState.value = playerUiState.value.copy(
                            isBuffering = false,
                            playbackState = PlaybackState.PLAYING
                        )
                    } else {
                        playerUiState.value = playerUiState.value.copy(
                            isBuffering = false,
                            playbackState = PlaybackState.PAUSED
                        )
                    }

                    val mediaItemDuration =
                        mediaController?.duration?.div(1000)?.toInt()?.formatDuration()
                    mediaItemDuration?.let { duration ->
                        playerUiState.value = playerUiState.value.copy(
                            trackDuration = duration
                        )
                    }

                }

                Player.STATE_ENDED -> {
                    playerUiState.value = playerUiState.value.copy(
                        seekPosition = 1F,
                        isBuffering = false,
                        playbackState = PlaybackState.PAUSED
                    )
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            playerUiState.value = playerUiState.value.copy(
                playbackState = PlaybackState.ERROR,
                isBuffering = false
            )
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val state = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
            // Prevent play/pause UI state from flipping when buffering
            if (mediaController?.playbackState != Player.STATE_BUFFERING) {
                playerUiState.value = playerUiState.value.copy(
                    playbackState = state
                )
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let {
                Timber.i("Media item changed to -> ${it.localConfiguration?.uri} Reason -> $reason")
                // TODO: Update current Track object, Queue, Plying track index in queue ...
            }
        }
    }

    fun releaseMediaController(controllerFuture: ListenableFuture<MediaController>) {
        MediaController.releaseFuture(controllerFuture)
    }
}
