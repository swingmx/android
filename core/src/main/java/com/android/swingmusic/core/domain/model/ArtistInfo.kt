package com.android.swingmusic.core.domain.model

data class ArtistInfo(
    val albumsAndAppearances: AlbumsAndAppearances,
    val artist: ArtistExpanded,
    val tracks: List<Track>,
    val stats: List<StatItem> = emptyList()
)

data class StatItem(
    val type: String,
    val value: String,
    val text: String,
    val image: String?
)
