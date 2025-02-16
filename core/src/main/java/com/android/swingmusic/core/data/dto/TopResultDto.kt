package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class TopResultDto(
    @SerializedName("item")
    val resultItemDto: TopResultItemDto?,
    @SerializedName("type")
    val type: String?
)
