package com.android.swingmusic.database.domain.model

import com.android.swingmusic.core.domain.util.QueueSource

data class LastPlayedTrack(
    val trackHash: String,
    val indexInQueue: Int,
    val source: QueueSource,
    val lastPlayPositionMs: Long
)
