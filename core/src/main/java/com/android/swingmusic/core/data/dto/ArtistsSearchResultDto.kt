package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class ArtistsSearchResultDto(
    @SerializedName("more")
    val more: Boolean?,
    @SerializedName("results")
    val resultDto: List<ArtistResultDto>?
)
