package com.android.swingmusic.network.domain.model

data class LogTrackRequest(
    val duration: Int,
    val source: String,
    val timestamp: Long,
    val trackhash: String
)
