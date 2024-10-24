package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class ArtistInfoDto(
    @SerializedName("albums")
    val albumsAndAppearancesDto: AlbumsAndAppearancesDto?,
    @SerializedName("artist")
    val artistExpandedDto: ArtistExpandedDto?,
    @SerializedName("tracks")
    val tracks: List<TrackDto>?
)
