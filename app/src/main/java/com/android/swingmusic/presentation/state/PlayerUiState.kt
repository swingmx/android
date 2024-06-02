package com.android.swingmusic.presentation.state

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode

data class PlayerUiState(
    val track: Track? = null,
    val playingTrackIndex: Int = 0,
    val queue: List<Track> = emptyList(),
    val playbackState: PlaybackState = PlaybackState.PAUSED,
    val seekPosition: Float = 0.0F,
    val playbackDuration: String = "00:00",
    val trackDuration: String = "00:00",
    val isBuffering: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.REPEAT_OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.SHUFFLE_OFF
)
