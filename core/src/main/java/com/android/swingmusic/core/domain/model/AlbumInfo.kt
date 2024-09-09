package com.android.swingmusic.core.domain.model

data class AlbumInfo(
    val albumArtists: List<Artist>,
    val albumHash: String,
    val artistHashes: List<String>,
    val baseTitle: String,
    val color: String,
    val createdDate: Int,
    val date: Long,
    val duration: Int,
    val favUserIds: List<Any>,
    val genreHashes: String,
    val genres: List<Genre>,
    val id: Int,
    val image: String,
    val isFavorite: Boolean,
    val lastPlayed: Int,
    val ogTitle: String,
    val playCount: Int,
    val playDuration: Int,
    val title: String,
    val trackCount: Int,
    val type: String,
    val versions: List<String>
)
