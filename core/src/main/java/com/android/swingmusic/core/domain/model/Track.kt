package com.android.swingmusic.core.domain.model


data class Track(
    val album: String?,
    val albumArtists: List<Artist>,
    val albumHash: String,
    val artistHashes: String,
    val artists: List<Artist>,
    val ati: String,
    val bitrate: Int,
    val copyright: String,
    val createdDate: Double,
    val date: Int,
    val disc: Int,
    val duration: Int,
    val filepath: String,
    val folder: String,
    val genre: List<String>,
    val image: String,
    val isFavorite: Boolean,
    val lastMod: Int,
    val ogAlbum: String,
    val ogTitle: String,
    val pos: Int,
    val title: String,
    val track: Int,
    val trackHash: String
)
