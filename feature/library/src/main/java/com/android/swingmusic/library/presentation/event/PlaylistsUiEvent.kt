package com.android.swingmusic.library.presentation.event

internal sealed interface PlaylistsUiEvent {
    data object OnScreenResumed : PlaylistsUiEvent
    data object OnBackClicked : PlaylistsUiEvent
    data object OnRetry : PlaylistsUiEvent
    data class OnPlaylistClicked(val playlistId: String) : PlaylistsUiEvent
    data object OnNewPlaylistClicked : PlaylistsUiEvent
    data class OnNewPlaylistNameChanged(val name: String) : PlaylistsUiEvent
    data object OnCreatePlaylist : PlaylistsUiEvent
    data object OnNewPlaylistDismissed : PlaylistsUiEvent
}
