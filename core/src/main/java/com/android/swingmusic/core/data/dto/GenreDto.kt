package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class GenreDto(
    @SerializedName("genrehash")
    val genreHash: String?,
    @SerializedName("name")
    val name: String?
)
