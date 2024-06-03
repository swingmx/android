package com.android.swingmusic.presentation.event

import com.android.swingmusic.core.domain.model.Track

interface QueueEvent {

    data class CreateNewQueue(
        val newQueue: List<Track>,
        val startIndex: Int,
        val autoPlay: Boolean
    ) : QueueEvent

    data class InsertTrackAtIndex(val track: Track, val index: Int) : QueueEvent

    data class SeekToQueueItem(val index: Int): QueueEvent

    object PlaUpNextTrack: QueueEvent

    object GetQueueFromDB : QueueEvent

    object ClearQueue : QueueEvent
}
