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
import com.android.swingmusic.player.domain.repository.PlayerRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.yield
import timber.log.Timber
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.roundToInt
import androidx.core.net.toUri


@HiltViewModel
class MediaControllerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val authRepository: AuthRepository,
    private val vibrator: Vibrator,
    private val settings: AppSettingsRepository,
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

    private var queueExpansionJob: Job? = null
    private var currentExpandingSource: QueueSource? = null

    private val _playerUiState: MutableStateFlow<PlayerUiState> = MutableStateFlow(
        PlayerUiState(
            nowPlayingTrack = null,
            queue = emptyList()
        )
    )
    val playerUiState: StateFlow<PlayerUiState> get() = _playerUiState

    init {
        viewModelScope.launch {
            val saved = settings.repeatMode.first()
            _playerUiState.update { it.copy(repeatMode = saved) }
            mediaController?.repeatMode = saved.toPlayerRepeatMode()
        }
        refreshBaseUrl()
    }

    fun refreshBaseUrl() {
        viewModelScope.launch {
            _baseUrl.update { authRepository.getBaseUrl() }
        }
    }

    /** Stops playback and forgets the queue when the user's session ends. */
    fun endSession() {
        cancelQueueExpansion()
        clearedQueueSnapshot = null
        trackToLog = null
        workingQueue.clear()
        shuffledQueue.clear()

        mediaController?.stop()
        mediaController?.clearMediaItems()

        _baseUrl.update { null }
        _playerUiState.update { PlayerUiState(repeatMode = it.repeatMode) }
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

            val savedQueue = playerRepository.getSavedQueue()
            val lastPlayedTrack = playerRepository.getLastPlayedTrack()

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
                        // Paused on reconnect: seed the UI state directly. A
                        // play()/pause() toggle here is async and can race,
                        // intermittently leaving a paused track playing (e.g. when
                        // the player is first expanded after reconnect).
                        _playerUiState.value = _playerUiState.value.copy(
                            playbackState = PlaybackState.PAUSED
                        )
                    }

                    currentMediaItemId?.let {
                        val index = mediaController?.currentMediaItemIndex ?: it.toInt()
                        if (index in workingQueue.indices) {
                            _playerUiState.value = _playerUiState.value.copy(
                                queue = workingQueue.toList(),
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
                            Timber.d("Reconnected on a different track than the saved one")
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
                            queue = workingQueue.toList(),
                            nowPlayingTrack = workingQueue[lastPlayedTrackIndex],
                            playingTrackIndex = lastPlayedTrackIndex,
                            seekPosition = seekTo,
                            playbackDuration = playbackDuration.toInt().formatDuration(),
                            trackDuration = trackDuration.formatDuration()
                        )

                        trackToLog = workingQueue[lastPlayedTrackIndex]

                    } else { // track index is out of range but queue is not empty
                        _playerUiState.value = _playerUiState.value.copy(
                            queue = workingQueue.toList(),
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
                playerRepository.insertQueue(queue)
                if (playingTrackIndex in queue.indices) {
                    savePlayingTrackToDatabase(
                        track = queue[playingTrackIndex],
                        source = _playerUiState.value.source,
                        indexInQueue = playingTrackIndex,
                        ignorePlayPosition = true
                    )
                }
            } catch (e: Exception) {
                Timber.tag("PLAYER").e(e, "Couldn't save the queue")
            }
        }
    }

    private fun createMediaItem(id: Int, track: Track): MediaItem {
        val encodedFilePath = Uri.encode(track.filepath)
        val uriString = "${_baseUrl.value}file/${track.trackHash}/legacy?filepath=$encodedFilePath"
        val uri = uriString.toUri()

        val artworkUri = "${_baseUrl.value}img/thumbnail/${track.image}".toUri()
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

    private fun addToPlayNextInQueue(tracks: List<Track>, source: QueueSource) {
        if (tracks.isEmpty()) return

        val targetQueue = if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
            shuffledQueue
        } else {
            workingQueue
        }

        if (targetQueue.isEmpty()) {
            if (workingQueue.isEmpty() && mediaController != null) {
                mediaController?.addListener(playerListener)
            }

            workingQueue = tracks.toMutableList()
            shuffledQueue.clear()

            loadMediaItems(
                tracks = workingQueue,
                startIndex = 0,
                autoPlay = false
            )

            mediaController?.prepare()

            _playerUiState.value = _playerUiState.value.copy(
                queue = workingQueue.toList(),
                playingTrackIndex = 0,
                nowPlayingTrack = tracks.first(),
                shuffleMode = ShuffleMode.SHUFFLE_OFF,
                source = source
            )

            trackToLog = tracks.first()
        } else {
            val currentPlayingIndex = mediaController?.currentMediaItemIndex ?: 0
            val insertIndex = currentPlayingIndex + 1

            val newQueue = targetQueue.toMutableList().apply { addAll(insertIndex, tracks) }
            if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
                shuffledQueue = newQueue
            } else {
                workingQueue = newQueue
            }
            _playerUiState.value = _playerUiState.value.copy(queue = newQueue)

            val mediaItems = newQueue.mapIndexed { index, track ->
                createMediaItem(id = index, track = track)
            }

            mediaController?.replaceMediaItems(
                insertIndex,
                newQueue.lastIndex,
                mediaItems.drop(insertIndex)
            )

            updateQueueInDatabase(
                queue = newQueue,
                playingTrackIndex = currentPlayingIndex
            )
        }
    }

    private fun addToPlayingQueue(tracks: List<Track>, source: QueueSource) {
        if (tracks.isEmpty()) return

        val targetQueue = if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
            shuffledQueue
        } else {
            workingQueue
        }

        if (targetQueue.isEmpty()) {
            if (mediaController != null) {
                mediaController?.addListener(playerListener)
            }

            workingQueue = tracks.toMutableList()
            shuffledQueue.clear()

            loadMediaItems(
                tracks = workingQueue,
                startIndex = 0,
                autoPlay = false
            )

            // Prepare the MediaController so it's ready to play
            mediaController?.prepare()

            _playerUiState.value = _playerUiState.value.copy(
                queue = workingQueue.toList(),
                playingTrackIndex = 0,
                nowPlayingTrack = tracks.first(),
                shuffleMode = ShuffleMode.SHUFFLE_OFF,
                source = source
            )

            trackToLog = tracks.first()
        } else {
            val firstNewIndex = targetQueue.size
            val newQueue = targetQueue.toMutableList().apply { addAll(tracks) }
            if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
                shuffledQueue = newQueue
            } else {
                workingQueue = newQueue
            }
            _playerUiState.value = _playerUiState.value.copy(queue = newQueue)

            val newMediaItems = tracks.mapIndexed { index, track ->
                createMediaItem(id = firstNewIndex + index, track = track)
            }

            mediaController?.addMediaItems(newMediaItems)

            updateQueueInDatabase(
                queue = newQueue,
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
                    // The server reads mixes as "mix:<id>.<sourcehash>" to list them in Recently played.
                    is QueueSource.MIX -> if (source.sourceHash.isBlank()) "mix:${source.id}"
                    else "mix:${source.id}.${source.sourceHash}"
                    is QueueSource.SEARCH -> "q:query"
                    is QueueSource.FAVORITE -> "favorite"
                    is QueueSource.UNKNOWN -> ""
                    else -> ""
                }

                if (durationPlayedSec >= 5) {
                    playerRepository.logLastPlayedTrackToServer(
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
                            playerRepository.updateLastPlayedTrack(
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
            Timber.tag("PLAYER").e(e, "Couldn't save the track progress")
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
                playerRepository.removeTrackFromFavorite(trackHash)
            } else {
                playerRepository.addTrackToFavorite(trackHash)
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
        // Central soft haptic for committed, state-changing actions.
        when (event) {
            is OnTogglePlayerState, is OnNext, is OnPrev, is OnSeekPlayBack,
            is OnToggleFavorite, is OnToggleRepeatMode,
            is PlayerUiEvent.OnToggleShuffleMode, is OnResumePlaybackFromError ->
                softHaptic()

            else -> Unit
        }

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

                    // seekTo keeps playWhenReady, so a paused track stays paused. The
                    // index overload also works before a restored track is prepared,
                    // when seeking within the current item isn't available yet.
                    if (controller.playbackState == Player.STATE_IDLE) controller.prepare()
                    controller.seekTo(controller.currentMediaItemIndex, seekPosition)
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
                    val next = when (_playerUiState.value.repeatMode) {
                        RepeatMode.REPEAT_OFF -> RepeatMode.REPEAT_ALL
                        RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                        RepeatMode.REPEAT_ONE -> RepeatMode.REPEAT_OFF
                    }
                    controller.repeatMode = next.toPlayerRepeatMode()
                    _playerUiState.value = _playerUiState.value.copy(repeatMode = next)
                    viewModelScope.launch { settings.setRepeatMode(next) }
                }

                is PlayerUiEvent.OnToggleShuffleMode -> {
                    viewModelScope.launch {
                        val shuffleMode = _playerUiState.value.shuffleMode
                        val newShuffleMode = if (shuffleMode == ShuffleMode.SHUFFLE_ON)
                            ShuffleMode.SHUFFLE_OFF else ShuffleMode.SHUFFLE_ON

                        if (event.toggleShuffle && workingQueue.isNotEmpty()) {
                            // Toggling while something plays: reorder around the playing track
                            reorderQueueInPlace(shuffle = newShuffleMode == ShuffleMode.SHUFFLE_ON)
                        } else if (newShuffleMode == ShuffleMode.SHUFFLE_ON || event.toggleShuffle.not()) {
                            // A new queue started with Shuffle: play it from a random track. The
                            // transition that follows logs the previous play, so don't log it here.
                            shuffledQueue = workingQueue.shuffled().toMutableList()

                            _playerUiState.value = _playerUiState.value.copy(
                                shuffleMode = ShuffleMode.SHUFFLE_ON,
                                nowPlayingTrack = shuffledQueue[0],
                                queue = shuffledQueue.toList()
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
                                queue = workingQueue.toList()
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

    private data class ClearedQueueSnapshot(
        val tracks: List<Track>,
        val index: Int,
        val source: QueueSource,
        val wasPlaying: Boolean
    )

    // Holds the last cleared queue so a drag-to-dismiss can be undone.
    private var clearedQueueSnapshot: ClearedQueueSnapshot? = null

    fun restoreClearedQueue() {
        val snapshot = clearedQueueSnapshot ?: return
        clearedQueueSnapshot = null
        if (snapshot.tracks.isEmpty()) return
        createNewQueue(
            tracks = snapshot.tracks,
            startIndex = snapshot.index.coerceIn(0, snapshot.tracks.lastIndex),
            autoPlay = snapshot.wasPlaying,
            source = snapshot.source
        )
    }

    fun onQueueEvent(event: QueueEvent) {
        when (event) {
            is QueueEvent.RecreateQueue -> {
                cancelQueueExpansion()
                
                // Capture the OLD source BEFORE any updates
                val oldSource = _playerUiState.value.source
                val oldQueueSize = _playerUiState.value.queue.size
                
                if (
                    event.source == oldSource &&
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

                if (event.source is QueueSource.FOLDER) {
                    val needsExpansion = if (oldSource == event.source) {
                        oldQueueSize != event.queue.size
                    } else {
                        true
                    }
                    
                    if (needsExpansion) {
                        _playerUiState.update { it.copy(isPartialQueue = true) }
                        startQueueExpansion(event.source, event.queue)
                    }
                }

                stabilizeSeekBarProgress()
            }

            is QueueEvent.SeekToQueueItem -> {
                mediaController?.prepare()
                mediaController?.seekTo(event.index, 0L)
                mediaController?.playWhenReady = true
                softHaptic()
            }

            is QueueEvent.PlayNext -> {
                addToPlayNextInQueue(event.tracks, event.source)
                activateHapticResponse()
            }

            is QueueEvent.MoveQueueItem -> moveQueueItem(event.from, event.to)

            is QueueEvent.RemoveQueueItem -> {
                removeQueueItem(event.index)
                softHaptic()
            }

            is QueueEvent.AddToQueue -> {
                addToPlayingQueue(event.tracks, event.source)
                activateHapticResponse()
            }

            is QueueEvent.ClearQueue -> {
                // Snapshot the queue first so the clear can be undone.
                val current = _playerUiState.value
                clearedQueueSnapshot = if (current.queue.isNotEmpty()) {
                    // Vibrant feedback for this destructive action.
                    destructiveHaptic()
                    ClearedQueueSnapshot(
                        // Defensive copy: _playerUiState.queue can be the same list
                        // instance as workingQueue, which clearQueue() empties in place.
                        tracks = current.queue.toList(),
                        index = current.playingTrackIndex,
                        source = current.source,
                        wasPlaying = current.playbackState == PlaybackState.PLAYING
                    )
                } else {
                    null
                }

                if (trackToLog != null && durationPlayedSec >= 5L) {
                    logRecentlyPlayedTrackToServer(
                        track = trackToLog,
                        durationPlayedSec = durationPlayedSec,
                        logReason = "CLEAR"
                    )
                }
                durationPlayedSec = 0L

                viewModelScope.launch {
                    playerRepository.clearQueue()

                    workingQueue.clear()
                    shuffledQueue.clear()
                    mediaController?.clearMediaItems()
                    trackToLog = null

                    _playerUiState.update { currentState ->
                        currentState.copy(
                            nowPlayingTrack = null,
                            queue = emptyList(),
                            playingTrackIndex = 0,
                            seekPosition = 0f,
                            playbackDuration = "00:00",
                            trackDuration = "00:00",
                            isBuffering = false,
                            playbackState = PlaybackState.PAUSED
                        )
                    }
                }
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
                        logRecentlyPlayedTrackToServer(
                            track = trackToLog,
                            durationPlayedSec = durationPlayedSec,
                            logReason = "END"
                        )

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

        // A loop (repeat one, or a one-track queue on repeat all) keeps the same media item,
        // so MediaController never reports a transition; log each completed loop here instead.
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (reason != Player.DISCONTINUITY_REASON_AUTO_TRANSITION) return
            if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) return

            if (trackToLog != null && durationPlayedSec >= 5L) {
                logRecentlyPlayedTrackToServer(
                    track = trackToLog,
                    durationPlayedSec = durationPlayedSec,
                    logReason = "LOOP"
                )
            }
            durationPlayedSec = 0L
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
                    // The timeline and the queue lists share an order, so the controller's
                    // index is the queue index (media ids were assigned by position and can be
                    // stale after a move or remove).
                    val trackIndex = mediaController?.currentMediaItemIndex ?: it.mediaId.toInt()
                    val currentQueue = if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
                        shuffledQueue
                    } else {
                        workingQueue
                    }
                    
                    // Bounds check to prevent crashes
                    if (trackIndex !in currentQueue.indices) {
                        Timber.e("Track index $trackIndex out of bounds for queue size ${currentQueue.size}")
                        return@let
                    }
                    
                    val playingTrack = currentQueue[trackIndex]

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
                    Timber.tag("PLAYER").e(e, "Media item transition failed")
                }
            }
        }
    }

    private fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
        RepeatMode.REPEAT_OFF -> Player.REPEAT_MODE_OFF
        RepeatMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
        RepeatMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
    }

    /**
     * Turns shuffle on or off without interrupting the playing track: the timeline is rebuilt
     * around the current item (move it first, replace the rest, move it back into place).
     */
    private fun reorderQueueInPlace(shuffle: Boolean) {
        val controller = mediaController ?: return
        val current = activeQueue()
        if (current.isEmpty() || controller.mediaItemCount == 0) return
        val currentIndex = controller.currentMediaItemIndex.coerceIn(0, current.lastIndex)
        val currentTrack = current[currentIndex]

        val newOrder: List<Track>
        val targetIndex: Int
        if (shuffle) {
            val rest = current.toMutableList().also { it.removeAt(currentIndex) }.shuffled()
            newOrder = listOf(currentTrack) + rest
            targetIndex = 0
            shuffledQueue = newOrder.toMutableList()
        } else {
            newOrder = workingQueue.toList()
            targetIndex = workingQueue.indexOfFirst { it === currentTrack }.takeIf { it >= 0 }
                ?: workingQueue.indexOfFirst { it.trackHash == currentTrack.trackHash }.coerceAtLeast(0)
        }

        val others = newOrder.filterIndexed { index, _ -> index != targetIndex }
        controller.moveMediaItem(currentIndex, 0)
        if (controller.mediaItemCount > 1) controller.removeMediaItems(1, controller.mediaItemCount)
        controller.addMediaItems(others.mapIndexed { index, track -> createMediaItem(id = index + 1, track = track) })
        if (targetIndex != 0) controller.moveMediaItem(0, targetIndex)

        _playerUiState.update { state ->
            state.copy(
                shuffleMode = if (shuffle) ShuffleMode.SHUFFLE_ON else ShuffleMode.SHUFFLE_OFF,
                queue = newOrder,
                playingTrackIndex = targetIndex,
                nowPlayingTrack = currentTrack
            )
        }
        updateQueueInDatabase(queue = workingQueue, playingTrackIndex = targetIndex)
    }

    private fun activeQueue(): MutableList<Track> =
        if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) shuffledQueue else workingQueue

    private fun moveQueueItem(from: Int, to: Int) {
        val queue = activeQueue()
        if (from == to || from !in queue.indices || to !in queue.indices) return
        queue.add(to, queue.removeAt(from))
        mediaController?.moveMediaItem(from, to)
        syncQueueState()
    }

    private fun removeQueueItem(index: Int) {
        val queue = activeQueue()
        if (index !in queue.indices) return
        val removed = queue.removeAt(index)
        if (queue !== workingQueue) workingQueue.remove(removed)
        mediaController?.removeMediaItem(index)
        syncQueueState()
    }

    /** Publishes the active queue as a new list and saves it, after a move or remove. */
    private fun syncQueueState() {
        val queue = activeQueue()
        val playingIndex = (mediaController?.currentMediaItemIndex ?: 0)
            .coerceIn(0, (queue.size - 1).coerceAtLeast(0))
        _playerUiState.update { state ->
            state.copy(
                queue = queue.toList(),
                playingTrackIndex = playingIndex,
                nowPlayingTrack = queue.getOrNull(playingIndex) ?: state.nowPlayingTrack
            )
        }
        updateQueueInDatabase(queue = workingQueue, playingTrackIndex = playingIndex)
    }

    private fun addTracksToCurrentQueue(newTracks: List<Track>) {
        val currentQueueSize = if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
            shuffledQueue.size
        } else {
            workingQueue.size
        }
        
        workingQueue.addAll(newTracks)
        
        val tracksToAddToMediaController = if (_playerUiState.value.shuffleMode == ShuffleMode.SHUFFLE_ON) {
            val shuffledNewTracks = newTracks.shuffled()
            shuffledQueue.addAll(shuffledNewTracks)
            shuffledNewTracks
        } else {
            newTracks
        }

        _playerUiState.update { state ->
            state.copy(
                queue = (if (state.shuffleMode == ShuffleMode.SHUFFLE_ON) shuffledQueue else workingQueue).toList()
            )
        }

        val mediaItems = tracksToAddToMediaController.mapIndexed { index, track ->
            createMediaItem(id = currentQueueSize + index, track = track)
        }
        
        mediaController?.addMediaItems(mediaItems)
        
        updateQueueInDatabase(
            queue = workingQueue.toList(),
            playingTrackIndex = _playerUiState.value.playingTrackIndex
        )
    }

    private fun startQueueExpansion(folderSource: QueueSource.FOLDER, currentTracks: List<Track>) {
        cancelQueueExpansion()
        currentExpandingSource = folderSource
        
        queueExpansionJob = viewModelScope.launch {
            try {
                var start = currentTracks.size
                val chunkSize = 50
                
                while (true) {
                    val newTracks = playerRepository.getTracksChunk(
                        folderPath = folderSource.path,
                        start = start,
                        limit = chunkSize
                    )
                    
                    if (newTracks.isEmpty()) break
                    
                    if (currentExpandingSource == folderSource && 
                        _playerUiState.value.source == folderSource) {
                        
                        addTracksToCurrentQueue(newTracks)
                    } else {
                        break
                    }
                    
                    start += chunkSize
                    yield()
                }
                
                if (currentExpandingSource == folderSource) {
                    _playerUiState.update { 
                        it.copy(isPartialQueue = false) 
                    }
                }
                
            } catch (e: CancellationException) {
                // Silent cancellation when switching folders
            } catch (e: Exception) {
                Timber.e("Queue Expansion Failed: $e")
            }
        }
    }

    private fun activateHapticResponse() {
        val timings = longArrayOf(0, 30, 60, 30)
        val amplitudes = intArrayOf(0, 30, 0, 30)

        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }

    /** A single soft tick for committed, state-changing player actions. */
    private fun softHaptic() {
        val effect = VibrationEffect.createOneShot(18, 30)
        vibrator.vibrate(effect)
    }

    /** A pronounced double-buzz for destructive actions (e.g. clearing the queue). */
    private fun destructiveHaptic() {
        val timings = longArrayOf(0, 50, 60, 90)
        val amplitudes = intArrayOf(0, 200, 0, 255)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }

    private fun cancelQueueExpansion() {
        queueExpansionJob?.cancel()
        queueExpansionJob = null
        currentExpandingSource = null
    }

    fun releaseMediaController(controllerFuture: ListenableFuture<MediaController>) {
        MediaController.releaseFuture(controllerFuture)
        mediaController?.release()
        mediaController = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelQueueExpansion()
    }
}
