package com.android.swingmusic.core.domain.model

data class ArtistInfo(
    val albumsAndAppearances: AlbumsAndAppearances,
    val artist: ArtistExpanded,
    val tracks: List<Track>,
    val stats: List<ArtistStat> = emptyList()
)

data class ArtistStat(
    val type: String,
    val value: String,
    val text: String,
    val image: String?
)
