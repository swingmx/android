package com.android.swingmusic.network.data.dto

import com.google.gson.annotations.SerializedName

data class AlbumHashRequestDto(
    @SerializedName("albumhash")
    val albumHash: String
)
