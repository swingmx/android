package com.android.swingmusic.album.presentation.event

interface AlbumWithInfoUiEvent {

    data class OnLoadAlbumWithInfo(val albumHash: String) : AlbumWithInfoUiEvent

    object OnRefreshAlbumInfo : AlbumWithInfoUiEvent

    object ResetState: AlbumWithInfoUiEvent

    data class OnToggleAlbumFavorite(
        val isFavorite: Boolean,
        val albumHash: String,
    ) : AlbumWithInfoUiEvent
}
