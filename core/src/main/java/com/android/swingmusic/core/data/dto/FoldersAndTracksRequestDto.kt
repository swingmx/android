package com.android.swingmusic.core.data.dto

import com.google.gson.annotations.SerializedName

data class FoldersAndTracksRequestDto(
    @SerializedName("folder")
    val folder: String?,
    @SerializedName("tracks_only")
    val tracksOnly: Boolean?,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("start")
    val start: Int,
)
