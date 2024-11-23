package com.android.swingmusic.core.domain.model


data class Track(
    val album: String,
    val albumTrackArtists: List<TrackArtist>,
    val albumHash: String,
    val trackArtists: List<TrackArtist>,
    val bitrate: Int,
    val duration: Int,
    val filepath: String,
    val folder: String,
    val image: String,
    val isFavorite: Boolean,
    val title: String,
    val trackHash: String,
    val disc: Int,
    val trackNumber: Int,
)
