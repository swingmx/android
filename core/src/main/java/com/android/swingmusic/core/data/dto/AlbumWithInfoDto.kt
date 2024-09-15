package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class AlbumWithInfoDto(
    @SerializedName("info")
    val albumInfoDto: AlbumInfoDto?,
    @SerializedName("tracks")
    val tracks: List<TrackDto>?,
    @SerializedName("copyright")
    val copyright: String?
)
