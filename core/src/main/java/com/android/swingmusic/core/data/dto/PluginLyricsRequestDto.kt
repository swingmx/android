package com.android.swingmusic.core.data.dto

import com.google.gson.annotations.SerializedName

data class PluginLyricsRequestDto(
    @SerializedName("trackhash")
    val trackhash: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("artist")
    val artist: String,
    @SerializedName("filepath")
    val filepath: String,
    @SerializedName("album")
    val album: String
)
