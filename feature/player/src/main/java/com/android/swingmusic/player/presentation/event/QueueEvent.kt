package com.android.swingmusic.player.presentation.event

import com.android.swingmusic.core.domain.model.Track

interface QueueEvent {

    data class RecreateQueue(
        val source: String, // Todo: Use an umbrella type for album, artist, folder, playList, search, favorite, Empty String
        val queue: List<Track>,
        val clickedTrackIndex: Int,
    ) : QueueEvent

    data class InsertTrackAtIndex(val track: Track, val index: Int) : QueueEvent

    data class SeekToQueueItem(val index: Int) : QueueEvent

    object ClearQueue : QueueEvent
}
