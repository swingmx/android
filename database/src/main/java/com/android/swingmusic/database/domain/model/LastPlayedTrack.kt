package com.android.swingmusic.database.domain.model

data class LastPlayedTrack(
    val trackHash: String,
    val indexInQueue: Int,
    val lastPlayPositionMs: Long
)
