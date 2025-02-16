package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class TracksSearchResultDto(
    @SerializedName("more")
    val more: Boolean?,
    @SerializedName("results")
    val results: List<TrackResultDto>?
)
