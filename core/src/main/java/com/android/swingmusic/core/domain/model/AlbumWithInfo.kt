package com.android.swingmusic.core.domain.model

data class AlbumWithInfo(
    val albumInfo: AlbumInfo,
    val tracks: List<Track>,
    val copyright: String
)
