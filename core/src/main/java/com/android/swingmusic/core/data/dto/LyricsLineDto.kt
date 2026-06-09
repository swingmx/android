package com.android.swingmusic.core.data.dto

import com.google.gson.annotations.SerializedName

data class LyricsLineDto(
    @SerializedName("time")
    val time: Double?,
    @SerializedName("text")
    val text: String?
)
