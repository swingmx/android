package com.android.swingmusic.player.presentation.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.player.domain.repository.QueueRepository
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnClickLyricsIcon
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnClickMore
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnClickQueue
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnNext
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnPrev
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnResumePlaybackFromError
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnSeekPlayBack
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnToggleFavorite
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnTogglePlayerState
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnToggleRepeatMode
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnToggleShuffleMode
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.state.PlayerUiState
import com.android.swingmusic.uicomponent.presentation.util.formatDuration
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MediaControllerViewModel @Inject constructor(
    private val queueRepository: QueueRepository
) : ViewModel() {
    private val playerListener = PlayerListener()
    private var mediaController: MediaController? = null
    fun getMediaController() = mediaController

    fun setMediaController(controller: MediaController) {
        if (mediaController == null) {
            mediaController = controller

            initPlayingListener()
            initQueue()
        }
    }

    fun reconnectMediaController(controller: MediaController) {
        mediaController = controller

        initPlayingListener()
        initQueue()

        // Quickly Trigger playing listener
        if (mediaController?.isPlaying == true) {
            mediaController?.pause()
            mediaController?.play()
        } else {
            mediaController?.play()
            mediaController?.pause()
        }

        Timber.e("--------------- RECONNECTED CONTROLLER TO SESSION ----------------")
    }

    private var workingQueue: MutableList<Track> = mutableListOf<Track>()
    private var shuffledQueue: MutableList<Track> = mutableListOf<Track>()

    val playerUiState: MutableState<PlayerUiState> = mutableStateOf(
        PlayerUiState(
            nowPlayingTrack = null,
            queue = workingQueue
        )
    )

    private var trackToLog: Track? = null
    private var queueSource: QueueSource = QueueSource.FOLDER // TODO: Use QueueSource enum class
    private var durationPlayed: Long = 0L
    private val playbackMutex = Mutex()

    private fun initPlayingListener() {
        viewModelScope.launch {
            mediaController?.let { controller ->
                controller.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        viewModelScope.launch {
                            if (isPlaying) {
                                playbackMutex.withLock {
                                    while (controller.isPlaying) {
                                        durationPlayed += 1
                                        updatePlaybackProgress(controller)
                                        delay(1000L)
                                    }
                                }
                            }
                        }

                        val state = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                        // Prevent play/pause UI state from flipping when buffering
                        if (mediaController?.playbackState != Player.STATE_BUFFERING) {
                            playerUiState.value = playerUiState.value.copy(
                                playbackState = state
                            )
                        }
                    }
                })
            }
        }
    }

    private fun updatePlaybackProgress(controller: Player) {
        if (controller.playbackState == Player.STATE_READY) {
            val exoPlayerPosition = controller.currentPosition
            val seekPosition = (exoPlayerPosition / 1000F)
                .div(playerUiState.value.nowPlayingTrack?.duration ?: 1)
                .coerceIn(0F, 1F)
            val playbackDuration = (exoPlayerPosition / 1000F).roundToInt()

            playerUiState.value = playerUiState.value.copy(
                seekPosition = seekPosition,
                playbackDuration = playbackDuration.formatDuration()
            )
        }
    }

    private fun initQueue() {
        viewModelScope.launch {
            val queue = queueRepository.getAllTracks()
            val lastPlayedTrack = queueRepository.getLastPlayedTrack()
            val trackIndex = lastPlayedTrack?.indexInQueue ?: -1

            // Init the working queue
            workingQueue = queue.toMutableList()

            if (queue.isNotEmpty() && trackIndex > -1) {

                /** It's now safe to add [PlayerListener] since it has a queue to work with */
                mediaController?.addListener(playerListener)
                if (mediaController?.mediaItemCount == 0) {
                    loadMediaItems(
                        tracks = workingQueue,
                        startIndex = if (trackIndex in queue.indices) trackIndex else 0,
                        autoPlay = false,
                        updateDatabase = false
                    )
                }

                if (trackIndex in queue.indices) {
                    playerUiState.value = playerUiState.value.copy(
                        queue = workingQueue,
                        nowPlayingTrack = workingQueue[trackIndex],
                        playingTrackIndex = trackIndex
                    )

                    trackToLog = workingQueue[trackIndex]
                } else { // track index is out of range but queue is not empty
                    playerUiState.value = playerUiState.value.copy(
                        queue = workingQueue,
                        nowPlayingTrack = workingQueue[0],
                        playingTrackIndex = 0
                    )

                    trackToLog = workingQueue[0]
                }

                stabilizeSeekBarProgress()
            }
        }
    }

    private fun updateQueueInDatabase(
        queue: List<Track>,
        playingTrackIndex: Int
    ) {
        viewModelScope.launch {
            if (queue.isEmpty()) return@launch
            try {
                queueRepository.insertTracks(queue)
                Timber.e("Queue Size: ${queue.size}")

                if (playingTrackIndex in queue.indices) {
                    val track = queue[playingTrackIndex]
                    queueRepository.updateLastPlayedTrack(
                        trackHash = track.trackHash,
                        indexInQueue = playingTrackIndex
                    )
                }
            } catch (e: Exception) {
                // Handle the exception, e.g., log it or show a user-friendly message
                Timber.e("Database Error")
            }
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
                    .setMediaType(MEDIA_TYPE_MUSIC)
                    .build()
            ).build()
    }

    /** Prevent seekbar from snapping to end by resetting it to zero
     * especially when switching shuffle mode or when creating new queue
     * **/
    private fun stabilizeSeekBarProgress() {
        playerUiState.value = playerUiState.value.copy(seekPosition = 0F)
    }

    private fun loadMediaItems(
        tracks: List<Track>,
        startIndex: Int = 0,
        autoPlay: Boolean = false,
        updateDatabase: Boolean = true
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
                seekToDefaultPosition(startIndex)
                playWhenReady = autoPlay
            }

            playerUiState.value = playerUiState.value.copy(
                playbackState = if (autoPlay) PlaybackState.PLAYING else PlaybackState.PAUSED
            )

            // Update db here because this function has the most recent queue
            if (updateDatabase) {
                updateQueueInDatabase(
                    queue = tracks,
                    playingTrackIndex = startIndex
                )
            }
        }
    }

    private fun createNewQueue(
        tracks: List<Track>,
        startIndex: Int,
        source: QueueSource = QueueSource.FOLDER, // TODO: Confirm possible sources
        autoPlay: Boolean
    ) {
        /** If working queue is empty at this point, then it means no queue was found in the db
         * which also means [PlayerListener] is not added yet */

        if (workingQueue.isEmpty() && mediaController != null) {
            mediaController?.addListener(playerListener)
        }

        workingQueue = tracks.toMutableList()
        shuffledQueue.clear()

        loadMediaItems(
            tracks = workingQueue,
            startIndex = startIndex,
            autoPlay = autoPlay
        )

        playerUiState.value = playerUiState.value.copy(
            playingTrackIndex = startIndex,
            nowPlayingTrack = workingQueue[startIndex],
            shuffleMode = ShuffleMode.SHUFFLE_OFF
        )

        // Update the Track to Log in case of a transition
        trackToLog = workingQueue[startIndex]
        queueSource = source
    }

    // TODO: Log the respective Track
    fun logRecentlyPlayedTrackToServer(
        track: Track?,
        durationPlayed: Long,
        logReason: String? = ""
    ) {
        track?.let {
            Timber.tag("LOG")
            Timber.e("[$logReason]: Played -> ${it.title} -> $durationPlayed sec")
        }
    }

    fun saveLastPlayedTrack(track: Track?, indexInQueue: Int) {
        track?.let {
            viewModelScope.launch {
                if (indexInQueue in workingQueue.indices) {
                    queueRepository.updateLastPlayedTrack(
                        trackHash = track.trackHash,
                        indexInQueue = indexInQueue
                    )
                }
            }
        }
    }

    fun onPlayerUiEvent(event: PlayerUiEvent) {
        mediaController?.let { controller ->
            when (event) {
                is OnSeekPlayBack -> {
                    val toValue = event.value // Ranges from 0.0 to 1.0
                    val playbackDuration =
                        (toValue * (playerUiState.value.nowPlayingTrack?.duration ?: 1)).toInt()

                    playerUiState.value = playerUiState.value.copy(
                        seekPosition = event.value,
                        playbackDuration = playbackDuration.formatDuration()
                    )
                    val trackDurationMs =
                        (playerUiState.value.nowPlayingTrack?.duration ?: 0) * 1000
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

                is OnResumePlaybackFromError -> {
                    controller.prepare()
                    controller.playWhenReady = true
                }

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
                        loadMediaItems(
                            tracks = shuffledQueue,
                            autoPlay = true,
                            startIndex = 0
                        )
                        playerUiState.value = playerUiState.value.copy(
                            shuffleMode = newShuffleMode,
                            nowPlayingTrack = shuffledQueue[0],
                            queue = shuffledQueue
                        )

                        trackToLog = shuffledQueue[0]
                    } else {
                        loadMediaItems(
                            tracks = workingQueue,
                            autoPlay = true,
                            startIndex = 0
                        )
                        playerUiState.value = playerUiState.value.copy(
                            shuffleMode = newShuffleMode,
                            nowPlayingTrack = workingQueue[0],
                            queue = workingQueue
                        )

                        trackToLog = workingQueue[0]
                    }

                    stabilizeSeekBarProgress()
                }

                is OnClickQueue -> {}

                is OnClickMore -> {}

                else -> {}
            }
        }
    }

    fun onQueueEvent(event: QueueEvent) {
        when (event) {
            is QueueEvent.GetQueueFromDB -> {
                initQueue()
            }

            is QueueEvent.RecreateQueue -> {
                // TODO: Check source with when()
                if (
                    event.source == playerUiState.value.nowPlayingTrack?.folder &&
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

                stabilizeSeekBarProgress()
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

                    updateQueueInDatabase(
                        queue = shuffledQueue,
                        playingTrackIndex = mediaController?.currentMediaItemIndex ?: 0
                    )
                } else {
                    workingQueue.add(event.index, eventTrack)
                    val mediaItem = createMediaItem(event.index, eventTrack)
                    mediaController?.addMediaItem(event.index, mediaItem)

                    updateQueueInDatabase(
                        queue = workingQueue,
                        playingTrackIndex = mediaController?.currentMediaItemIndex ?: 0
                    )
                }
            }

            is QueueEvent.ClearQueue -> {
                viewModelScope.launch {
                    queueRepository.clearQueue()

                    workingQueue.clear()
                    shuffledQueue.clear()
                    mediaController?.clearMediaItems()
                    trackToLog = null
                }
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

                    if (trackToLog != null && durationPlayed > 5L) {
                        logRecentlyPlayedTrackToServer(
                            track = trackToLog,
                            durationPlayed = durationPlayed,
                            logReason = "E"
                        )

                        saveLastPlayedTrack(
                            track = trackToLog,// == playerUiState.value.track
                            indexInQueue = playerUiState.value.playingTrackIndex
                        )
                    }

                    // Prepare for the next log data
                    durationPlayed = 0L
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            playerUiState.value = playerUiState.value.copy(
                playbackState = PlaybackState.ERROR,
                isBuffering = false
            )
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
                    nowPlayingTrack = playingTrack
                )

                if (trackToLog != null && durationPlayed > 5L) {
                    logRecentlyPlayedTrackToServer(
                        track = trackToLog,
                        durationPlayed = durationPlayed,
                        logReason = "T"
                    )

                    saveLastPlayedTrack(
                        track = trackToLog,// == playerUiState.value.track
                        indexInQueue = trackIndex // == playerUiState.value.playingTrackIndex
                    )
                }

                // Prepare for the next log data
                durationPlayed = 0L
                trackToLog = playingTrack
            }
        }
    }

    fun releaseMediaController(controllerFuture: ListenableFuture<MediaController>) {
        MediaController.releaseFuture(controllerFuture)
        mediaController?.release()
        mediaController = null
    }
}
