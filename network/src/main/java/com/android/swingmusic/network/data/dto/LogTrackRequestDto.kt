package com.android.swingmusic.network.data.dto

import com.google.gson.annotations.SerializedName

data class LogTrackRequestDto(
    val duration: Int,
    val source: String,
    val timestamp: Long,
    @SerializedName("trackhash")
    val trackHash: String
)
