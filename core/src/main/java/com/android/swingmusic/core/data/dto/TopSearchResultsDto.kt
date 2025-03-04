package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class TopSearchResultsDto(
    @SerializedName("top_result")
    val topResultItemDto: TopResultItemDto?,
    @SerializedName("albums")
    val albumsDto: List<AlbumDto>?,
    @SerializedName("artists")
    val artistsDto: List<ArtistDto>?,
    @SerializedName("tracks")
    val tracksDto: List<TrackDto>?,
)
