package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class AllArtistsDto(
    @SerializedName("items")
    val artists: List<ArtistDto>?,
    @SerializedName("total")
    val total: Int?
)
