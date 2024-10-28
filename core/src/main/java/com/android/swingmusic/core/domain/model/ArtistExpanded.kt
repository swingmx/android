package com.android.swingmusic.core.domain.model

data class ArtistExpanded(
    val albumCount: Int,
    val artistHash: String,
    val color: String,
    val duration: Int,
    val genres: List<Genre>,
    val image: String,
    val isFavorite: Boolean,
    val name: String,
    val trackCount: Int
)
