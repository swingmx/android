package com.android.swingmusic.core.data.dto

import com.google.gson.annotations.SerializedName

data class PluginLyricsResultDto(
    @SerializedName("trackhash")
    val trackhash: String?,
    @SerializedName("lyrics")
    val lyrics: List<LyricsLineDto>?,
    @SerializedName("error")
    val error: String?
)
