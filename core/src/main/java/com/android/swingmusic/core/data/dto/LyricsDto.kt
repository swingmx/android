package com.android.swingmusic.core.data.dto

import com.google.gson.annotations.SerializedName

data class LyricsDto(
    @SerializedName("error")
    val error: Boolean?,
    @SerializedName("synced")
    val synced: Boolean?,
    @SerializedName("lyrics")
    val lyrics: List<LyricsLineDto>?,
    @SerializedName("copyright")
    val copyright: String?
)
