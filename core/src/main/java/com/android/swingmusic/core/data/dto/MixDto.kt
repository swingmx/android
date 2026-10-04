package com.android.swingmusic.core.data.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class MixDto(
    @SerializedName("id")
    val id: JsonElement?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("sourcehash")
    val sourceHash: String?,
    @SerializedName("trackcount")
    val trackCount: Int?,
    @SerializedName("extra")
    val extra: MixExtraDto?
)

data class MixExtraDto(
    @SerializedName("type")
    val type: String?,
    @SerializedName("og_sourcehash")
    val ogSourceHash: String?,
    @SerializedName("image")
    val image: MixImageDto?,
    @SerializedName("images")
    val images: List<MixImageDto>?
)

data class MixImageDto(
    @SerializedName("image")
    val image: String?,
    @SerializedName("color")
    val color: String?,
    @SerializedName("type")
    val type: String?
)

data class MixTracksDto(
    @SerializedName("tracks")
    val tracks: List<TrackDto>?
)
