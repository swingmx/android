package com.android.swingmusic.core.domain.model

data class ArtistInfo(
    val albumsAndAppearances: AlbumsAndAppearances,
    val artist: ArtistExpanded,
    val tracks: List<Track>,
    val stats: List<StatItem> = emptyList(),
    val listening: Listening? = null
)

/** All-time listening for one artist or album by the current user. */
data class Listening(
    val playDuration: Int,
    val playCount: Int,
    val lastPlayedAgo: String?,
    val rank: ListeningRank?,
    val topAlbum: ListeningTopAlbum?,
    val topTrack: ListeningTopTrack?
)

data class ListeningRank(val position: Int, val total: Int, val of: String)

data class ListeningTopAlbum(
    val albumHash: String,
    val title: String,
    val image: String,
    val artist: String?,
    val playDuration: Int
)

data class ListeningTopTrack(val trackHash: String, val title: String, val image: String, val playDuration: Int)

data class StatItem(
    val type: String,
    val value: String,
    val text: String,
    val image: String?
)
