package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class AlbumDto(
    @SerializedName("albumartists")
    val albumArtistDto: List<ArtistDto>?,
    @SerializedName("albumhash")
    val albumHash: String?,
    @SerializedName("colors")
    val colors: List<String>?,
    @SerializedName("created_date")
    val createdDate: Double?,
    @SerializedName("date")
    val date: Int?,
    @SerializedName("help_text")
    val helpText: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("versions")
    val versions: List<String>?
)
