package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class ArtistInfoDto(
    @SerializedName("albums")
    val albumsAndAppearancesDto: AlbumsAndAppearancesDto?,
    @SerializedName("artist")
    val artistExpandedDto: ArtistExpandedDto?,
    @SerializedName("tracks")
    val tracks: List<TrackDto>?,
    @SerializedName("stats")
    val stats: List<ArtistStatDto>?
)

data class ArtistStatDto(
    @SerializedName("cssclass")
    val cssClass: String?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("image")
    val image: String?
)
