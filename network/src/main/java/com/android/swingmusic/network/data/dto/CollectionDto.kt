package com.android.swingmusic.network.data.dto

import com.google.gson.annotations.SerializedName

data class CollectionDto(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("items")
    val items: List<CollectionItemDto>?,
    @SerializedName("extra")
    val extra: CollectionExtraDto?
)

data class CollectionExtraDto(
    @SerializedName("description")
    val description: String?
)

/** An item in a collection: an album, artist, mix or playlist by hash. */
data class CollectionItemDto(
    @SerializedName("type")
    val type: String,
    @SerializedName("hash")
    val hash: String
)

/** Body of POST/DELETE collections/{id}/items. */
data class CollectionItemRequestDto(
    @SerializedName("item")
    val item: CollectionItemDto
)

data class CreateCollectionRequestDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("items")
    val items: List<CollectionItemDto>
)
