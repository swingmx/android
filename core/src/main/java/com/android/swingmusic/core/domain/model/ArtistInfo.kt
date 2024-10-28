package com.android.swingmusic.core.domain.model

data class ArtistInfo(
    val albumsAndAppearances: AlbumsAndAppearances,
    val artist: ArtistExpanded,
    val tracks: List<Track>
)
