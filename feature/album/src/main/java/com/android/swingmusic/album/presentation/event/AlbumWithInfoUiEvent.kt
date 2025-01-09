package com.android.swingmusic.album.presentation.event

interface AlbumWithInfoUiEvent {

    data class OnLoadAlbumWithInfo(val albumHash: String) : AlbumWithInfoUiEvent

    data class OnUpdateAlbumHash(val albumHash: String) : AlbumWithInfoUiEvent

    object OnRefreshAlbumInfo : AlbumWithInfoUiEvent

    object ResetState : AlbumWithInfoUiEvent

    data class OnToggleAlbumFavorite(
        val isFavorite: Boolean,
        val albumHash: String,
    ) : AlbumWithInfoUiEvent

    data class OnToggleAlbumTrackFavorite(
        val trackHash: String,
        val favorite: Boolean
    ) : AlbumWithInfoUiEvent
}
