package com.android.swingmusic.player.presentation.event

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.QueueSource

interface QueueEvent {

    data class RecreateQueue(
        val source: QueueSource,
        val queue: List<Track>,
        val clickedTrackIndex: Int,
    ) : QueueEvent

    data class PlayNext(val track: Track) : QueueEvent

    data class AddToQueue(val track: Track) : QueueEvent

    data class SeekToQueueItem(val index: Int) : QueueEvent

    object ClearQueue : QueueEvent

    data class ShowSnackbar(val msg: String, val actionLabel: String) : QueueEvent

    object HideSnackbar : QueueEvent
}
