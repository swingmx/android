package com.android.swingmusic.library.presentation.event

internal sealed class LibraryUiEffect {
    data object NavigateToProfile : LibraryUiEffect()
    data object NavigateToPlaylists : LibraryUiEffect()
    data object NavigateToAlbums : LibraryUiEffect()
    data object NavigateToArtists : LibraryUiEffect()
    data object NavigateToFolders : LibraryUiEffect()
}
