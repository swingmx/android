package com.android.swingmusic.album.presentation.event

interface AlbumWithInfoUiEvent {

    data class OnLoadAlbumWithInfo(val albumHash: String) : AlbumWithInfoUiEvent

    object OnRefreshAlbumInfo : AlbumWithInfoUiEvent

    data class OnToggleAlbumFavorite(
        val isFavorite: Boolean,
        val albumHash: String,
    ) : AlbumWithInfoUiEvent
}
