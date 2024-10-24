package com.android.swingmusic.core.data.dto


import com.android.swingmusic.core.data.dto.GenreDto
import com.google.gson.annotations.SerializedName

data class ArtistExpandedDto(
    @SerializedName("albumcount")
    val albumcount: Int?,
    @SerializedName("artisthash")
    val artisthash: String?,
    @SerializedName("color")
    val color: String?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("genres")
    val genres: List<GenreDto>?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("is_favorite")
    val isFavorite: Boolean?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("trackcount")
    val trackcount: Int?
)
