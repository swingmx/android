package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class AllAlbumsDto(
    @SerializedName("items")
    val albumDto: List<AlbumDto>?,
    @SerializedName("total")
    val total: Int?
)
