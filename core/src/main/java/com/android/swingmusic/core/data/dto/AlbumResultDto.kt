package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class AlbumResultDto(
    @SerializedName("albumartists")
    val albumArtists: List<ArtistDto>?,
    @SerializedName("albumhash")
    val albumHash: String?,
    @SerializedName("color")
    val color: String?,
    @SerializedName("date")
    val date: Int?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("versions")
    val versions: List<Any>?
)
