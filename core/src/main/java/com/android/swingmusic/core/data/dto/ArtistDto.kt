package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class ArtistDto(
    @SerializedName("artisthash")
    val artisthash: String?,
    @SerializedName("colors")
    val colors: List<String>?,
    @SerializedName("created_date")
    val createdDate: Double?,
    @SerializedName("help_text")
    val helpText: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("name")
    val name: String?
)