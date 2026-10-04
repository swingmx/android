package com.android.swingmusic.core.data.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class PlaylistDto(
    @SerializedName("id")
    val id: JsonElement?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("has_image")
    val hasImage: Boolean?,
    @SerializedName("images")
    val images: List<PlaylistImageDto>?,
    @SerializedName("trackcount")
    val trackCount: Int?,
    @SerializedName("count")
    val count: Int?
)

data class PlaylistImageDto(
    @SerializedName("image")
    val image: String?
)

data class PlaylistTracksDto(
    @SerializedName("tracks")
    val tracks: List<TrackDto>?
)
