package com.android.swingmusic.core.data.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class HomeSectionDto(
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("items")
    val items: List<HomeItemDto>?
)

data class HomeItemDto(
    @SerializedName("type")
    val type: String?,
    @SerializedName("item")
    val item: JsonElement?
)
