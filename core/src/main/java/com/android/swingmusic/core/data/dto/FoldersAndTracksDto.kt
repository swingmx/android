package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class FoldersAndTracksDto(
    @SerializedName("folders")
    val foldersDto: List<FolderDto>?,
    @SerializedName("tracks")
    val tracksDto: List<TrackDto>?
)
