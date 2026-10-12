package com.android.swingmusic.core.data.dto

import com.google.gson.annotations.SerializedName

data class FavouritesDto(
    @SerializedName("tracks")
    val tracks: List<TrackDto>?,
    @SerializedName("albums")
    val albums: List<AlbumDto>?,
    @SerializedName("artists")
    val artists: List<ArtistDto>?,
    @SerializedName("count")
    val count: FavouriteCountDto?
)

data class FavouriteCountDto(
    @SerializedName("tracks")
    val tracks: Int?,
    @SerializedName("albums")
    val albums: Int?,
    @SerializedName("artists")
    val artists: Int?
)

/** [total] is only filled in when the page starts at 0; later pages return -1. */
data class FavouriteTracksDto(
    @SerializedName("tracks")
    val tracks: List<TrackDto>?,
    @SerializedName("total")
    val total: Int?
)

data class FavouriteAlbumsDto(
    @SerializedName("albums")
    val albums: List<AlbumDto>?,
    @SerializedName("total")
    val total: Int?
)

data class FavouriteArtistsDto(
    @SerializedName("artists")
    val artists: List<ArtistDto>?,
    @SerializedName("total")
    val total: Int?
)
