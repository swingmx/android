package com.android.swingmusic.player.presentation.viewmodel

import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.player.domain.repository.PLayerRepository
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnClickLyricsIcon
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnClickMore
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnNext
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnPrev
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnResumePlaybackFromError
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnRetry
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnSeekPlayBack
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnToggleFavorite
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnTogglePlayerState
import com.android.swingmusic.player.presentation.event.PlayerUiEvent.OnToggleRepeatMode
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.state.PlayerUiState
import com.android.swingmusic.uicomponent.presentation.util.formatDuration
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class MediaControllerViewModel @Inject constructor(
    private val pLayerRepository: PLayerRepository,
    private val authRepository: AuthRepository,
    private val vibrator: Vibrator
) : ViewModel() {
    private val _baseUrl: MutableStateFlow<String?> = MutableStateFlow(null)
    val baseUrl: StateFlow<String?> get() = _baseUrl

    private val playerListener = PlayerListener()
    private var mediaController: MediaController? = null
    private var workingQueue: MutableList<Track> = mutableListOf()
    private var shuffledQueue: MutableList<Track> = mutableListOf()
    private var trackToLog: Track? = null

    private var durationPlayedSec: Long = 0L
    private val playbackMutex = Mutex()

    private val _playerUiState: MutableStateFlow<PlayerUiState> = MutableStateFlow(
        PlayerUiState(
            nowPlayingTrack = null,
            queue = emptyList()
        )
    )
    val playerUiState: StateFlow<PlayerUiState> get() = _playerUiState

    init {
        refreshBaseUrl()
    }

    fun refreshBaseUrl() {
        _baseUrl.value = authRepository.getBaseUrl()
    }

    fun getMediaController() = mediaController
    fun setMediaController(controller: MediaController) {
        if (mediaController == null) {
            mediaController = controller

            initQueue()
        }
    }

    fun reconnectMediaController(controller: MediaController) {
        mediaController = controller

        val repeatMode = when (mediaController?.repeatMode) {
            Player.REPEAT_MODE_ALL -> RepeatMode.REPEAT_ALL
            Player.REPEAT_MODE_ONE -> RepeatMode.REPEAT_ONE
            Player.REPEAT_MODE_OFF -> RepeatMode.REPEAT_OFF
            else -> RepeatMode.REPEAT_OFF
        }
        durationPlayedSec = (controller.currentPosition.div(1000))
        _playerUiState.value = _playerUiState.value.copy(repeatMode = repeatMode)
        initQueue(isOnSessionReconnect = true)
    }

    private fun updatePlaybackProgress(controller: Player) {
        if (controller.playbackState == Player.STATE_READY) {
            val playerPosition = controller.currentPosition
            val seekPosition = (playerPosition / 1000F)
                .div(_playerUiState.value.nowPlayingTrack?.duration ?: 1)
                .coerceIn(0F, 1F)
            val playbackDuration = (playerPosition / 1000F).roundToInt()

            _playerUiState.value = _playerUiState.value.copy(
                seekPosition = seekPosition,
                playbackDuration = playbackDuration.formatDuration()
            )

            if (durationPlayedSec >= 5 && durationPlayedSec.mod(5) == 0) {
                // Hit db every 5 seconds the player is playing
                savePlayingTrackToDatabase(
                    track = _playerUiState.value.nowPlayingTrack,
                    source = _playerUiState.value.source,
                    indexInQueue = _playerUiState.value.playingTrackIndex
                )
            }
        }
    }

    private fun initQueue(isOnSessionReconnect: Boolean = false) {
        viewModelScope.launch {
            refreshBaseUrl()

            val savedQueue = pLayerRepository.getSavedQueue()
            val lastPlayedTrack = pLayerRepository.getLastPlayedTrack()

            val lastPlayedTrackIndex = lastPlayedTrack?.indexInQueue ?: -1
            val lastPlayPositionMs = lastPlayedTrack?.lastPlayPositionMs ?: 0L
            val lastPlayedQueueSource = lastPlayedTrack?.source ?: QueueSource.UNKNOWN

            _playerUiState.value = _playerUiState.value.copy(source = lastPlayedQueueSource)

            // Init the working queue
            workingQueue = savedQueue.toMutableList()

            if (isOnSessionReconnect) {
                if (lastPlayedTrackIndex in savedQueue.indices) {
                    mediaController?.addListener(playerListener)

                    val currentMediaItemId = mediaController?.currentMediaItem?.mediaId

                    // Quickly trigger PlayState listener
                    if (mediaController?.isPlaying == true) {
                        mediaController?.pause()
                        mediaController?.play()
                    } else {
                        mediaController?.play()
                        mediaController?.pause()
                    }

                    currentMediaItemId?.let {
                        val index = it.toInt()
                        if (index in workingQueue.indices) {
                            _playerUiState.value = _playerUiState.value.copy(
                                queue = workingQueue,
                                nowPlayingTrack = workingQueue[index], // id == "index"
                                playingTrackIndex = index,
                                seekPosition =
                                mediaController
                                    ?.contentPosition
                                    ?.div(1000F)
                                    ?.div(workingQueue[index].duration)
                                    ?: 0F
                            )
                        }

                        if (index != lastPlayedTrackIndex) {
                            Timber.e("ON SESSION RECONNECTION: INDEX != LAST PLAYED  TRACK INDEX")
                            savePlayingTrackToDatabase(
                                track = workingQueue[index],
                                source = lastPlayedQueueSource,
                                indexInQueue = index
                            )
                        }

                        trackToLog = workingQueue[index]
                    }
                }
            } else {
                if (savedQueue.isNotEmpty() && lastPlayedTrackIndex > -1) {
                    /** It's now safe to add [PlayerListener] since it has a queue to work with */
                    mediaController?.addListener(playerListener)

                    if (mediaController?.mediaItemCount == 0) {
                        loadMediaItems(
                            tracks = workingQueue,
                            startIndex = if (lastPlayedTrackIndex in savedQueue.indices) lastPlayedTrackIndex else 0,
                            autoPlay = false,
                            updateDatabase = false
                        )
                        mediaController?.seekTo(lastPlayedTrackIndex, lastPlayPositionMs)
                    }

                    if (lastPlayedTrackIndex in savedQueue.indices) {
                        val seekTo =
                            lastPlayPositionMs.div(1000F) / workingQueue[lastPlayedTrackIndex].duration
                        val trackDuration = workingQueue[lastPlayedTrackIndex].duration
                        val playbackDuration = seekTo * trackDuration

                        _playerUiState.value = _playerUiState.value.copy(
                            queue = workingQueue,
                            nowPlayingTrack = workingQueue[lastPlayedTrackIndex],
                            playingTrackIndex = lastPlayedTrackIndex,
                            seekPosition = seekTo,
                            playbackDuration = playbackDuration.toInt().formatDuration(),
                            trackDuration = trackDuration.formatDuration()
                        )

                        trackToLog = workingQueue[lastPlayedTrackIndex]

                    } else { // track index is out of range but queue is not empty
                        _playerUiState.value = _playerUiState.value.copy(
                            queue = workingQueue,
                            nowPlayingTrack = workingQueue[0],
                            playingTrackIndex = 0
                        )

                        trackToLog = workingQueue[0]
                    }
                } else {
                    // Either the queue is empty or trackIndex is null -> Do Nothing
                    // This is reached if it is the first time launching the app or
                    // when no track has been played yet.
                }
            }
        }
    }


    fun initQueueFromGivenSource(tracks: List<Track>, source: QueueSource) {
        _playerUiState.value =
            _playerUiState.value.copy(source = source)
        workingQueue = tracks.toMutableList()
        shuffledQueue = mutableListOf()
    }

    private fun updateQueueInDatabase(
        queue: List<Track>,
        playingTrackIndex: Int
    ) {
        viewModelScope.launch {
            if (queue.isEmpty()) return@launch
            try {
                pLayerRepository.insertQueue(queue)
                if (playingTrackIndex in queue.indices) {
                    savePlayingTrackToDatabase(
                        track = queue[playingTrackIndex],
                        source = _playerUiState.value.source,
                        indexInQueue = playingTrackIndex,
                        ignorePlayPosition = true
                    )
                }
            } catch (e: Exception) {
                Timber.e("ERROR SAVING NEW QUEUE!")
            }
        }
    }

    private fun createMediaItem(id: Int, track: Track): MediaItem {
        val encodedFilePath = Uri.encode(track.filepath)
        val uriString = "${_baseUrl.value}file/${track.trackHash}/legacy?filepath=$encodedFilePath"
        val uri = Uri.parse(uriString)

        val artworkUri = Uri.parse("${_baseUrl.value}img/thumbnail/${track.image}")
        val artists = track.trackArtists.joinToString(", ") { it.name }

        val mediaMetadata = MediaMetadata.Builder()
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setTitle(track.title)
            .setArtworkUri(artworkUri)
            .setArtist(artists)
            .build()

        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(id.toString())
            .setMediaMetadata(mediaMetadata)
            .build()
    }

    /** Prevent seekbar from snapping to end by resetting it to zero
     * especially when switching shuffle mode or when creating new queue
     * **/
    private fun stabilizeSeekBarProgress() {
        _playerUiState.value = _playerUiState.value.copy(seekPosition = 0F)
    }

    private fun loadMediaItems(
        tracks: List<Track>,
        startIndex: Int = 0,
        autoPlay: Boolean = false,
        updateDatabase: Boolean = true
    ) {
        viewModelScope.launch {
            val mediaItems = tracks.mapIndexed { index, track ->
                createMediaItem(id = index, track = track)
            }
            val uiRepeatMode = when (_playerUiState.value.repeatMode) {
                RepeatMode.REPEAT_OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
                RepeatMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
            }
            mediaController?.apply {
                clearMediaItems()
                addMediaItems(mediaItems)
                setPlaybackSpeed(1F)
                repeatMode = uiRepeatMode
                seekToDefaultPosition(startIndex)
                if (autoPlay) prepare()
                playWhenReady = autoPlay
            }

            _playerUiState.value = _playerUiState.value.copy(
                playbackState = if (autoPlay) PlaybackState.PLAYING else PlaybackState.PAUSED
            )

            /** Update db here because [loadMediaItems] has the most recent queue */
            if (updateDatabase) {
                updateQueueInDatabase(
                    queue = tracks,
                    playingTrackIndex = startIndex
                )
            }
        }
    }

    private fun addToPlayNextInQueue(playNextTrack: Track?, source: QueueSource) {
        if (playNextTrack == null) return

        val targetQueue = if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
            shuffledQueue
        } else {
            workingQueue
        }

        if (targetQueue.isEmpty()) {
            mediaController?.addListener(playerListener)

            onQueueEvent(
                QueueEvent.RecreateQueue(
                    source = source,
                    queue = listOf(playNextTrack),
                    clickedTrackIndex = 0
                )
            )
        } else {
            val currentPlayingIndex = mediaController?.currentMediaItemIndex ?: -1
            val insertIndex = if (currentPlayingIndex < 0) 0 else currentPlayingIndex + 1

            targetQueue.add(insertIndex, playNextTrack)
            _playerUiState.value = _playerUiState.value.copy(queue = targetQueue)

            val mediaItems = targetQueue.mapIndexed { index, track ->
                createMediaItem(id = index, track = track)
            }

            mediaController?.replaceMediaItems(
                insertIndex,
                targetQueue.lastIndex,
                mediaItems.drop(insertIndex)
            )

            updateQueueInDatabase(
                queue = targetQueue,
                playingTrackIndex = mediaController?.currentMediaItemIndex ?: 0
            )
        }
    }

    private fun addToPlayingQueue(track: Track?, source: QueueSource) {
        if (track == null) return

        val targetQueue = if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
            shuffledQueue
        } else {
            workingQueue
        }

        if (targetQueue.isEmpty()) {
            mediaController?.addListener(playerListener)

            onQueueEvent(
                QueueEvent.RecreateQueue(
                    source = source,
                    queue = listOf(track),
                    clickedTrackIndex = 0
                )
            )
        } else {
            targetQueue.add(track)
            _playerUiState.value = _playerUiState.value.copy(queue = targetQueue)

            val newMediaItem = createMediaItem(
                id = targetQueue.lastIndex,
                track = track
            )

            mediaController?.apply {
                addMediaItem(newMediaItem)
            }

            updateQueueInDatabase(
                queue = targetQueue,
                playingTrackIndex = mediaController?.currentMediaItemIndex ?: 0
            )
        }
    }

    private fun createNewQueue(
        tracks: List<Track>,
        startIndex: Int,
        source: QueueSource,
        autoPlay: Boolean = true
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

        _playerUiState.value = _playerUiState.value.copy(
            queue = tracks,
            playingTrackIndex = startIndex,
            nowPlayingTrack = workingQueue[startIndex],
            shuffleMode = ShuffleMode.SHUFFLE_OFF
        )

        // Update the Track to Log in case of a Transition or Shuffle/End
        trackToLog = workingQueue[startIndex]
        _playerUiState.value = _playerUiState.value.copy(source = source)
    }

    fun logRecentlyPlayedTrackToServer(
        track: Track?,
        durationPlayedSec: Long,
        logReason: String? = ""
    ) {
        track?.let {
            viewModelScope.launch {
                Timber.tag("LOG")
                Timber.d("[$logReason]: Played -> [${it.title}] -> $durationPlayedSec sec -> Remote")

                val sourcePath = when (val source = _playerUiState.value.source) {
                    is QueueSource.ALBUM -> "al:${source.albumHash}"
                    is QueueSource.ARTIST -> "ar:${source.artistHash}"
                    is QueueSource.FOLDER -> "fo:${source.path}"
                    is QueueSource.PLAYLIST -> "pl:${source.id}"
                    is QueueSource.QUERY -> "q:${source.query}"
                    is QueueSource.FAVORITE -> "favorite"
                    is QueueSource.UNKNOWN -> ""
                    else -> ""
                }

                if (durationPlayedSec >= 5) {
                    pLayerRepository.logLastPlayedTrackToServer(
                        track,
                        durationPlayedSec.toInt(),
                        sourcePath
                    )
                } else {
                    Timber.tag("LOG")
                    Timber.d("[$logReason]: TRACK NOT LOGGED -> [${it.title}] -> PLAY DURATION $durationPlayedSec sec TOO SHORT")
                }
            }
        }
    }

    fun savePlayingTrackToDatabase(
        track: Track?,
        indexInQueue: Int,
        lastPlayPositionMs: Long? = null,
        source: QueueSource = QueueSource.UNKNOWN,
        ignorePlayPosition: Boolean = false
    ) {
        try {
            track?.let {
                val pos = lastPlayPositionMs ?: mediaController?.currentPosition ?: 0
                if (pos >= 5000L || ignorePlayPosition) {
                    Timber.tag("DATABASE")
                    Timber.d("Saving playing track progress: [${track.title}] @ ${pos.div(1000)} sec -> Local")

                    viewModelScope.launch {
                        if (indexInQueue in workingQueue.indices) {
                            pLayerRepository.updateLastPlayedTrack(
                                trackHash = track.trackHash,
                                indexInQueue = indexInQueue,
                                source = source,
                                lastPlayPositionMs = if (it.trackHash == trackToLog?.trackHash) pos else 0
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("ERROR SAVING TRACK PROGRESS CAUSED BY $e")
        }
    }

    private fun toggleTrackFavorite(trackHash: String, isFavorite: Boolean) {
        viewModelScope.launch {
            // Optimistically update the UI
            _playerUiState.value = _playerUiState.value.copy(
                nowPlayingTrack = _playerUiState.value.nowPlayingTrack?.copy(isFavorite = !isFavorite),
                queue = _playerUiState.value.queue.map { track ->
                    if (track.trackHash == trackHash) {
                        track.copy(isFavorite = !isFavorite)
                    } else {
                        track
                    }
                }
            )

            // Optimistically update Queue
            workingQueue = workingQueue.map { track ->
                if (track.trackHash == trackHash) {
                    track.copy(isFavorite = !isFavorite)
                } else {
                    track
                }
            }.toMutableList()

            shuffledQueue = shuffledQueue.map { track ->
                if (track.trackHash == trackHash) {
                    track.copy(isFavorite = !isFavorite)
                } else {
                    track
                }
            }.toMutableList()

            val request = if (isFavorite) {
                pLayerRepository.removeTrackFromFavorite(trackHash)
            } else {
                pLayerRepository.addTrackToFavorite(trackHash)
            }

            request.collectLatest {
                when (it) {
                    is Resource.Loading -> {}

                    is Resource.Success -> {
                        if (_playerUiState.value.nowPlayingTrack?.trackHash == trackHash) {
                            _playerUiState.value = _playerUiState.value.copy(
                                nowPlayingTrack = _playerUiState.value.nowPlayingTrack
                                    ?.copy(isFavorite = it.data ?: false)
                            )
                        }
                    }

                    is Resource.Error -> {
                        // Revert the optimistic updates in case of an error
                        _playerUiState.value = _playerUiState.value.copy(
                            nowPlayingTrack = _playerUiState.value.nowPlayingTrack?.copy(isFavorite = isFavorite),
                            queue = _playerUiState.value.queue.map { track ->
                                if (track.trackHash == trackHash) {
                                    track.copy(isFavorite = isFavorite)
                                } else {
                                    track
                                }
                            }
                        )

                        workingQueue = workingQueue.map { track ->
                            if (track.trackHash == trackHash) {
                                track.copy(isFavorite = isFavorite)
                            } else {
                                track
                            }
                        }.toMutableList()

                        shuffledQueue = shuffledQueue.map { track ->
                            if (track.trackHash == trackHash) {
                                track.copy(isFavorite = isFavorite)
                            } else {
                                track
                            }
                        }.toMutableList()
                    }
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
                        (toValue * (_playerUiState.value.nowPlayingTrack?.duration ?: 1)).toInt()

                    _playerUiState.value = _playerUiState.value.copy(
                        seekPosition = event.value,
                        playbackDuration = playbackDuration.formatDuration()
                    )
                    val trackDurationMs =
                        (_playerUiState.value.nowPlayingTrack?.duration ?: 0) * 1000
                    val positionInMs = (trackDurationMs * toValue).toLong()
                    val seekPosition = positionInMs.coerceIn(0, trackDurationMs.toLong())

                    if (controller.playbackState == Player.STATE_READY) {
                        controller.seekTo(seekPosition)
                        controller.play()
                    } else {
                        mediaController?.apply {
                            prepare()
                            seekTo(seekPosition)
                            play()
                        }
                    }
                }

                is OnPrev -> {
                    if (controller.hasPreviousMediaItem()) {
                        stabilizeSeekBarProgress()
                        _playerUiState.value = _playerUiState.value.copy(
                            playbackDuration = "00:00"
                        )
                    }

                    with(controller) {
                        prepare()
                        seekToPrevious()
                        play()
                    }
                }

                is OnNext -> {
                    if (controller.hasNextMediaItem()) {
                        stabilizeSeekBarProgress()
                        _playerUiState.value = _playerUiState.value.copy(
                            playbackDuration = "00:00"
                        )
                    }

                    with(controller) {
                        prepare()
                        seekToNext()
                        play()
                    }
                }

                is OnTogglePlayerState -> {
                    when (_playerUiState.value.playbackState) {
                        PlaybackState.PLAYING -> {
                            controller.pause()
                            _playerUiState.value = _playerUiState.value.copy(
                                playbackState = PlaybackState.PAUSED
                            )
                        }

                        /** UI remain paused for a moment during which the
                         *  player is buffering... [PlayerListener] will
                         *  update this UI state when the playback resumes.*/
                        PlaybackState.PAUSED -> {
                            controller.play()
                        }

                        else -> {}
                    }

                    when (controller.playbackState) {
                        Player.STATE_ENDED -> {
                            controller.prepare()
                            controller.seekTo(0, 0)
                            controller.play()
                        }

                        else -> {}
                    }
                }

                is OnResumePlaybackFromError -> {
                    controller.prepare()
                    controller.playWhenReady = true
                }

                is OnToggleFavorite -> {
                    toggleTrackFavorite(event.trackHash, event.isFavorite)
                }

                is OnClickLyricsIcon -> {}

                is OnToggleRepeatMode -> {
                    val repeatMode = _playerUiState.value.repeatMode

                    _playerUiState.value = _playerUiState.value.copy(
                        repeatMode = when (repeatMode) {
                            RepeatMode.REPEAT_OFF -> {
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

                is PlayerUiEvent.OnToggleShuffleMode -> {
                    logRecentlyPlayedTrackToServer(
                        track = trackToLog,
                        durationPlayedSec = durationPlayedSec,
                        logReason = "SHUFFLE ON [${_playerUiState.value.shuffleMode != ShuffleMode.SHUFFLE_ON}]"
                    )

                    viewModelScope.launch {
                        val shuffleMode = _playerUiState.value.shuffleMode
                        val newShuffleMode = if (shuffleMode == ShuffleMode.SHUFFLE_ON)
                            ShuffleMode.SHUFFLE_OFF else ShuffleMode.SHUFFLE_ON

                        // Handle shuffle mode manually (not directly via MediaController)
                        // Save 0th track immediately even in error state
                        if (newShuffleMode == ShuffleMode.SHUFFLE_ON || event.toggleShuffle.not()) {
                            shuffledQueue = workingQueue.shuffled().toMutableList()

                            _playerUiState.value = _playerUiState.value.copy(
                                shuffleMode = ShuffleMode.SHUFFLE_ON,
                                nowPlayingTrack = shuffledQueue[0],
                                queue = shuffledQueue
                            )
                            loadMediaItems(
                                tracks = shuffledQueue,
                                autoPlay = true,
                                startIndex = 0
                            )

                            savePlayingTrackToDatabase(
                                shuffledQueue[0],
                                0,
                                0L,
                                _playerUiState.value.source,
                                true
                            )
                            trackToLog = shuffledQueue[0]
                        } else {
                            _playerUiState.value = _playerUiState.value.copy(
                                shuffleMode = ShuffleMode.SHUFFLE_OFF,
                                nowPlayingTrack = workingQueue[0],
                                queue = workingQueue
                            )
                            loadMediaItems(
                                tracks = workingQueue,
                                autoPlay = true,
                                startIndex = 0
                            )

                            savePlayingTrackToDatabase(
                                workingQueue[0],
                                0,
                                0L,
                                _playerUiState.value.source,
                                true
                            )
                            trackToLog = workingQueue[0]
                        }
                    }

                    stabilizeSeekBarProgress()
                }

                is OnClickMore -> {}

                is OnRetry -> {
                    initQueue()
                }

                else -> {}
            }
        }
    }

    fun onQueueEvent(event: QueueEvent) {
        when (event) {
            is QueueEvent.RecreateQueue -> {
                if (
                    event.source == _playerUiState.value.source &&
                    (_playerUiState.value.playbackState != PlaybackState.ERROR) &&
                    (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_OFF) &&
                    (_playerUiState.value.queue == event.queue)
                ) {
                    // The queue hasn't changed or shuffled -> seekTo this index
                    onQueueEvent(QueueEvent.SeekToQueueItem(event.clickedTrackIndex))
                } else {
                    _playerUiState.value = _playerUiState.value.copy(
                        shuffleMode = ShuffleMode.SHUFFLE_OFF // please please please
                    )

                    createNewQueue(
                        tracks = event.queue,
                        startIndex = event.clickedTrackIndex,
                        autoPlay = true,
                        source = event.source
                    )
                }

                stabilizeSeekBarProgress()
            }

            is QueueEvent.SeekToQueueItem -> {
                mediaController?.prepare()
                mediaController?.seekTo(event.index, 0L)
                mediaController?.playWhenReady = true
            }

            is QueueEvent.PlayNext -> {
                addToPlayNextInQueue(event.track, event.source)
                activateHapticResponse()
            }

            is QueueEvent.AddToQueue -> {
                addToPlayingQueue(event.track, event.source)
                activateHapticResponse()
            }

            is QueueEvent.ClearQueue -> {
                viewModelScope.launch {
                    pLayerRepository.clearQueue()

                    workingQueue.clear()
                    shuffledQueue.clear()
                    mediaController?.clearMediaItems()
                    trackToLog = null
                }

                activateHapticResponse()
            }
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            mediaController?.let { controller ->
                viewModelScope.launch {
                    if (isPlaying) {
                        playbackMutex.withLock {
                            while (controller.isPlaying) {
                                updatePlaybackProgress(controller)
                                durationPlayedSec += 1
                                delay(1000L)
                            }
                        }
                    }
                }

                val state = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                // Prevent play/pause UI state from flipping when buffering
                if (controller.playbackState != Player.STATE_BUFFERING) {
                    _playerUiState.value = _playerUiState.value.copy(
                        playbackState = state
                    )
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {}

                Player.STATE_BUFFERING -> {
                    _playerUiState.value = _playerUiState.value.copy(
                        isBuffering = true
                    )
                }

                Player.STATE_READY -> {
                    if (mediaController?.isPlaying == true) {
                        _playerUiState.value = _playerUiState.value.copy(
                            isBuffering = false,
                            playbackState = PlaybackState.PLAYING
                        )
                    } else {
                        _playerUiState.value = _playerUiState.value.copy(
                            isBuffering = false,
                            playbackState = PlaybackState.PAUSED
                        )
                    }

                    val mediaItemDuration =
                        mediaController?.duration?.div(1000)?.toInt()?.formatDuration()
                    mediaItemDuration?.let { duration ->
                        _playerUiState.value = _playerUiState.value.copy(
                            trackDuration = duration
                        )
                    }

                }

                Player.STATE_ENDED -> {
                    _playerUiState.value = _playerUiState.value.copy(
                        seekPosition = 1F,
                        isBuffering = false,
                        playbackState = PlaybackState.PAUSED
                    )

                    if (trackToLog != null && durationPlayedSec >= 5L) {
                        if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_OFF) {
                            logRecentlyPlayedTrackToServer(
                                track = trackToLog,
                                durationPlayedSec = durationPlayedSec,
                                logReason = "END"
                            )
                        }

                        savePlayingTrackToDatabase(
                            track = _playerUiState.value.nowPlayingTrack,
                            source = _playerUiState.value.source,
                            indexInQueue = _playerUiState.value.playingTrackIndex
                        )
                    }

                    // Prepare for the next log data
                    durationPlayedSec = 0L
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playerUiState.value = _playerUiState.value.copy(
                playbackState = PlaybackState.ERROR,
                playbackDuration = "00:00",
                isBuffering = false
            )
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let {
                try {
                    val trackIndex = it.mediaId.toInt() // "id" == index
                    val playingTrack: Track =
                        if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
                            shuffledQueue[trackIndex]
                        } else {
                            workingQueue[trackIndex]
                        }

                    _playerUiState.value = _playerUiState.value.copy(
                        playingTrackIndex = trackIndex,
                        nowPlayingTrack = playingTrack,
                        trackDuration = playingTrack.duration.formatDuration()
                    )

                    if (trackToLog != null && durationPlayedSec >= 5L) {
                        logRecentlyPlayedTrackToServer(
                            track = trackToLog,
                            durationPlayedSec = durationPlayedSec,
                            logReason = "TRANSITION"
                        )

                        if (trackToLog?.trackHash == _playerUiState.value.nowPlayingTrack?.trackHash) {
                            savePlayingTrackToDatabase(
                                track = _playerUiState.value.nowPlayingTrack,
                                indexInQueue = trackIndex,
                                source = _playerUiState.value.source,
                                lastPlayPositionMs = durationPlayedSec * 1000L
                            )
                        }
                    }

                    // Prepare for the next Transition log data
                    durationPlayedSec = 0L
                    trackToLog = playingTrack
                } catch (e: Exception) {
                    Timber.e("ERROR ON TRANSITION -> $e")
                }
            }
        }
    }

    private fun activateHapticResponse() {
        val timings = longArrayOf(0, 30, 60, 30)
        val amplitudes = intArrayOf(0, 30, 0, 30)

        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }

    fun releaseMediaController(controllerFuture: ListenableFuture<MediaController>) {
        MediaController.releaseFuture(controllerFuture)
        mediaController?.release()
        mediaController = null
    }
}
