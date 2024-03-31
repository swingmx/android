package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class ArtistDto(
    @SerializedName("artisthash")
    val artistHash: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("name")
    val name: String?
)
