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
    val count: Int?,
    @SerializedName("pinned")
    val pinned: Boolean? = null,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("settings")
    val settings: PlaylistSettingsDto? = null
)

data class PlaylistSettingsDto(
    @SerializedName("has_gif")
    val hasGif: Boolean?,
    @SerializedName("banner_pos")
    val bannerPos: Int?,
    @SerializedName("square_img")
    val squareImg: Boolean?,
    @SerializedName("pinned")
    val pinned: Boolean?
)

data class PlaylistImageDto(
    @SerializedName("image")
    val image: String?
)

data class PlaylistTracksDto(
    @SerializedName("info")
    val info: PlaylistDto? = null,
    @SerializedName("tracks")
    val tracks: List<TrackDto>?
)

data class AllPlaylistsDto(
    @SerializedName("data")
    val data: List<PlaylistDto>?
)
