package com.android.swingmusic.player.presentation.event

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.QueueSource

interface QueueEvent {

    data class RecreateQueue(
        val source: QueueSource,
        val queue: List<Track>,
        val clickedTrackIndex: Int,
    ) : QueueEvent

    data class InsertTrackAtIndex(val track: Track, val index: Int) : QueueEvent

    data class SeekToQueueItem(val index: Int) : QueueEvent

    object ClearQueue : QueueEvent
}
