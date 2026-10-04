package com.android.swingmusic.core.domain.model

data class Mix(
    val id: String,
    val title: String,
    val description: String,
    val sourceHash: String,
    val ogSourceHash: String,
    val type: String,
    val image: String?,
    val color: String?,
    val images: List<MixImage>,
    val trackCount: Int
)

data class MixImage(
    val image: String,
    val isArtist: Boolean
)
