package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class TrackResultDto(
    @SerializedName("album")
    val album: String?,
    @SerializedName("albumartists")
    val albumArtists: List<ArtistDto>?,
    @SerializedName("albumhash")
    val albumHash: String?,
    @SerializedName("artisthashes")
    val artistHashes: List<String>?,
    @SerializedName("artists")
    val artists: List<TrackArtistDto>?,
    @SerializedName("bitrate")
    val bitrate: Int?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("filepath")
    val filepath: String?,
    @SerializedName("folder")
    val folder: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("is_favorite")
    val isFavorite: Boolean?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("trackhash")
    val trackHash: String?,
    @SerializedName("weakhash")
    val weakHash: String?
)
