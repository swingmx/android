package com.android.swingmusic.library.presentation.event

internal sealed interface LibraryUiEvent {
    data object OnScreenResumed : LibraryUiEvent
    data object OnProfileClicked : LibraryUiEvent
    data object OnPlaylistsClicked : LibraryUiEvent
    data object OnAlbumsClicked : LibraryUiEvent
    data object OnArtistsClicked : LibraryUiEvent
    data object OnFoldersClicked : LibraryUiEvent
    data class OnAlbumClicked(val albumHash: String) : LibraryUiEvent
}
