package com.android.swingmusic.player.presentation.state

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode

data class PlayerUiState(
    val nowPlayingTrack: Track? = null,
    val source: QueueSource = QueueSource.UNKNOWN,
    val playingTrackIndex: Int = 0,
    val queue: List<Track> = emptyList(),
    val playbackState: PlaybackState = PlaybackState.PAUSED,
    val seekPosition: Float = 0.0F,
    val playbackDuration: String = "00:00",
    val trackDuration: String = "00:00",
    val isBuffering: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.REPEAT_ALL,
    val shuffleMode: ShuffleMode = ShuffleMode.SHUFFLE_OFF,
    val isPartialQueue: Boolean = false
)
