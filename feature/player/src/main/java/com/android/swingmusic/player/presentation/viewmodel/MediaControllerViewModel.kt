package com.android.swingmusic.player.presentation.viewmodel

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
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnClickLyricsIcon
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnClickMore
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnClickQueue
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnNext
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnPlayBackComplete
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnPrev
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnSeekPlayBack
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnToggleFavorite
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnTogglePlayerState
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnToggleRepeatMode
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnToggleShuffleMode
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.state.PlayerUiState
import com.android.swingmusic.uicomponent.presentation.util.formatDuration
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MediaControllerViewModel : ViewModel() {
    private val playerListener = PlayerListener()
    private var mediaController: MediaController? = null
    fun getMediaController() = mediaController

    fun setMediaController(controller: MediaController) {
        if (mediaController == null)
            mediaController = controller
    }

    private var workingQueue: MutableList<Track> = mutableListOf<Track>()
    private var shuffledQueue: MutableList<Track> = mutableListOf<Track>()

    val playerUiState: MutableState<PlayerUiState> = mutableStateOf(
        PlayerUiState(
            track = null, // Get this fro DB at first launch
            queue = workingQueue
        )
    )

    init {
        viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    if (controller.playbackState == Player.STATE_READY) {
                        /*val exoPlayerPosition = when (val pos = controller.currentPosition) {
                            0L -> 1
                            else -> pos
                        }*/
                        val exoPlayerPosition = controller.currentPosition
                        val seekPosition = (exoPlayerPosition / 1000F)
                            .div(playerUiState.value.track?.duration ?: 1)
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

    init {
        /** TODO: Check if playerUiState.value.queue is Empty... if so,
         *        Get saved Track from DB (one time not a Flow),
         *        Update working queue with a single track,
         * */

        /* onQueueEvent(
             QueueEvent.CreateNewQueue(
                 newQueue = playerUiState.value.queue,
                 startIndex = 0,
                 autoPlay = true
             )
         )*/
        /*workingQueue = tracks.toMutableList()
        playerUiState.value = playerUiState.value.copy(
            queue = workingQueue,
            track = workingQueue[0]
        )*/

        if (playerUiState.value.track != null) {
            val trackDuration = playerUiState.value.track!!.duration.formatDuration()
            playerUiState.value = playerUiState.value.copy(
                trackDuration = trackDuration
            )
        }
    }

    private fun createMediaItem(index: Int, track: Track): MediaItem {
        val path = "$BASE_URL${""}file/${track.trackHash}?filepath=${track.filepath}"
        val encodedPath = Uri.encode(path, "/:;?&=+$,@!*()^.<>_-")
        val uri = Uri.parse(encodedPath)

        val artworkUri = Uri.parse("$BASE_URL${""}img/t/${track.image}")
        val artists = track.trackArtists.joinToString(", ") { it.name }

        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(index.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(artists)
                    .setTitle(track.title)
                    .setArtworkUri(artworkUri)
                    .setGenre(track.genre.joinToString(", ") { it })
                    .build()
            ).build()
    }

    public fun loadMediaItems() {
        if (playerUiState.value.queue.isNotEmpty()) {
            _loadMediaItems(playerUiState.value.queue)
        }
    }

    /** Prevent seekbar from snapping to end by resetting it to zero
     * especially when switching shuffle mode
     * **/
    private fun stabilizeSeekBarProgress() {
        playerUiState.value = playerUiState.value.copy(seekPosition = 0F)
    }

    private fun _loadMediaItems(
        tracks: List<Track>,
        startIndex: Int = 0,
        autoPlay: Boolean = false
    ) {
        viewModelScope.launch {
            val mediaItems = tracks.mapIndexed { index, track ->
                createMediaItem(index, track)
            }
            mediaController?.apply {
                clearMediaItems()
                addMediaItems(mediaItems)
                prepare()
                setPlaybackSpeed(1F)
                addListener(playerListener)
                seekToDefaultPosition(startIndex)
                playWhenReady = autoPlay

                playerUiState.value = playerUiState.value.copy(
                    playbackState = if (autoPlay) PlaybackState.PLAYING else PlaybackState.PAUSED
                )
            }
        }
    }

    fun onPlayerUiEvent(event: PlayerUiEvent) {
        mediaController?.let { controller ->
            when (event) {
                is OnSeekPlayBack -> {
                    val toValue = event.value // Ranges from 0.0 to 1.0
                    val playbackDuration =
                        (toValue * (playerUiState.value.track?.duration ?: 1)).toInt()

                    playerUiState.value = playerUiState.value.copy(
                        seekPosition = event.value,
                        playbackDuration = playbackDuration.formatDuration()
                    )
                    val trackDurationMs = (playerUiState.value.track?.duration ?: 0) * 1000
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

                    // TODO: Calculate +ve play time, update global value
                }

                is OnPrev -> {
                    controller.seekToPrevious()
                }

                is OnNext -> {
                    controller.seekToNext()
                    controller.playWhenReady = true
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
                    val newShuffleMode = if (shuffleMode == ShuffleMode.SHUFFLE_ON)
                        ShuffleMode.SHUFFLE_OFF else ShuffleMode.SHUFFLE_ON

                    // Handle shuffle mode manually
                    if (newShuffleMode == ShuffleMode.SHUFFLE_ON) {
                        shuffledQueue = workingQueue.shuffled().toMutableList()
                        _loadMediaItems(
                            tracks = shuffledQueue,
                            autoPlay = true,
                            startIndex = 0
                        )
                        playerUiState.value = playerUiState.value.copy(
                            shuffleMode = newShuffleMode,
                            track = shuffledQueue[0],
                            queue = shuffledQueue
                        )
                    } else {
                        _loadMediaItems(
                            tracks = workingQueue,
                            autoPlay = true,
                            startIndex = 0
                        )
                        playerUiState.value = playerUiState.value.copy(
                            shuffleMode = newShuffleMode,
                            track = workingQueue[0],
                            queue = workingQueue
                        )
                    }

                    stabilizeSeekBarProgress()
                }

                is OnClickQueue -> {}

                is OnClickMore -> {}

                else -> {}
            }
        }
    }

    private fun createNewQueue(tracks: List<Track>, startIndex: Int, autoPlay: Boolean) {
        workingQueue = tracks.toMutableList()
        shuffledQueue.clear()

        _loadMediaItems(
            tracks = workingQueue,
            startIndex = startIndex,
            autoPlay = autoPlay
        )

        playerUiState.value = playerUiState.value.copy(
            playingTrackIndex = startIndex,
            track = workingQueue[startIndex],
            shuffleMode = ShuffleMode.SHUFFLE_OFF
        )
    }

    fun onQueueEvent(event: QueueEvent) {
        when (event) {
            is QueueEvent.GetQueueFromDB -> {
                // TODO: Fetch Queue from Room
                // getQueueFromDB()
            }

            is QueueEvent.CreateQueueFromFolder -> {
                if (
                    event.folderPath == playerUiState.value.track?.folder &&
                    playerUiState.value.playbackState != PlaybackState.ERROR
                ) {
                    // The queue hasn't changed -> seekTo this index
                    mediaController?.seekTo(event.clickedTrackIndex, 0L)
                    mediaController?.playWhenReady = true

                    playerUiState.value = playerUiState.value.copy(
                        shuffleMode = ShuffleMode.SHUFFLE_OFF
                    )
                } else {
                    createNewQueue(
                        tracks = event.queue,
                        startIndex = event.clickedTrackIndex,
                        autoPlay = true
                    )
                }
            }

            is QueueEvent.CreateNewQueue -> {
                createNewQueue(
                    tracks = event.newQueue,
                    startIndex = event.startIndex,
                    autoPlay = event.autoPlay
                )
            }

            /**  This assumes the queue hasn't changed/shuffled/cleared  **/
            is QueueEvent.SeekToQueueItem -> {
                mediaController?.seekTo(event.index, 0L)
                mediaController?.playWhenReady = true
            }

            is QueueEvent.PlaUpNextTrack -> {
                mediaController?.seekToNext()
                mediaController?.playWhenReady = true
            }

            is QueueEvent.InsertTrackAtIndex -> {
                val eventTrack = event.track
                if (playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
                    shuffledQueue.add(event.index, eventTrack)
                    val mediaItem = createMediaItem(event.index, eventTrack)
                    mediaController?.addMediaItem(event.index, mediaItem)
                } else {
                    workingQueue.add(event.index, eventTrack)
                    val mediaItem = createMediaItem(event.index, eventTrack)
                    mediaController?.addMediaItem(event.index, mediaItem)
                }
            }

            is QueueEvent.ClearQueue -> {
                workingQueue.clear()
                shuffledQueue.clear()
                mediaController?.clearMediaItems()
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
                val trackIndex = it.mediaId.toInt() // index == id
                val playingTrack: Track =
                    if (playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
                        shuffledQueue[trackIndex]
                    } else {
                        workingQueue[trackIndex]
                    }

                playerUiState.value = playerUiState.value.copy(
                    playingTrackIndex = trackIndex,
                    track = playingTrack
                )
                // TODO: Push Logs to server referring to the previous track. check repeat mode or get prev media item.
                // TODO: Save this to DB as the last played track [pls don't save queue or index]
            }
        }
    }

    fun releaseMediaController(controllerFuture: ListenableFuture<MediaController>) {
        MediaController.releaseFuture(controllerFuture)
        mediaController?.release()
        mediaController = null
    }
}
