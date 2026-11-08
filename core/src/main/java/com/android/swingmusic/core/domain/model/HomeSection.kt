package com.android.swingmusic.core.domain.model

data class HomeSection(
    val key: String,
    val title: String,
    val description: String,
    val items: List<HomeItem>
)

sealed interface HomeItem {
    data class AlbumItem(val album: Album) : HomeItem
    data class ArtistItem(val artist: Artist) : HomeItem
    data class TrackItem(val track: Track) : HomeItem
    data class PlaylistItem(val playlist: Playlist) : HomeItem
    data class MixItem(val mix: Mix) : HomeItem
    data class FolderItem(val folder: Folder) : HomeItem
}

/** A recently favourited item and when it was favourited, e.g. "2 days ago". */
data class RecentFavourite(val item: HomeItem, val time: String)

