package com.android.swingmusic.core.domain.model

data class Album(
    val albumArtists: List<Artist>,
    val albumHash: String,
    val colors: List<String>,
    val createdDate: Double,
    val date: Int,
    val helpText: String,
    val image: String,
    val title: String,
    val versions: List<String>
)
