package com.android.swingmusic.core.data.dto

import com.google.gson.annotations.SerializedName

data class LyricsRequestDto(
    @SerializedName("filepath")
    val filepath: String,
    @SerializedName("trackhash")
    val trackhash: String
)
