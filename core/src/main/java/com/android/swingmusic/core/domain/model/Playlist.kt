package com.android.swingmusic.core.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val customImage: String?,
    val images: List<String>,
    val trackCount: Int
)
