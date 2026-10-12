package com.android.swingmusic.player.presentation.event

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.QueueSource

interface QueueEvent {

    data class RecreateQueue(
        val source: QueueSource,
        val queue: List<Track>,
        val clickedTrackIndex: Int,
        val isPartialQueue: Boolean = false
    ) : QueueEvent

    data class PlayNext(val tracks: List<Track>, val source: QueueSource) : QueueEvent {
        constructor(track: Track, source: QueueSource) : this(listOf(track), source)
    }

    data class AddToQueue(val tracks: List<Track>, val source: QueueSource) : QueueEvent {
        constructor(track: Track, source: QueueSource) : this(listOf(track), source)
    }

    data class SeekToQueueItem(val index: Int) : QueueEvent

    object ClearQueue : QueueEvent
}
