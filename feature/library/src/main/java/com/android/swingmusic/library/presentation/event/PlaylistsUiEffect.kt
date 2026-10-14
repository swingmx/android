package com.android.swingmusic.library.presentation.event

internal sealed class PlaylistsUiEffect {
    data object NavigateBack : PlaylistsUiEffect()
    data class NavigateToPlaylist(val playlistId: String) : PlaylistsUiEffect()
    data class ShowSnackbar(val message: String) : PlaylistsUiEffect()
}
