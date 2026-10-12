package com.android.swingmusic.playlist.presentation.event

import com.android.swingmusic.core.domain.model.Playlist

internal sealed interface AddToPlaylistUiEvent {
    data object OnRetry : AddToPlaylistUiEvent
    data class OnPlaylistClicked(val playlist: Playlist) : AddToPlaylistUiEvent
    data object OnNewPlaylistClicked : AddToPlaylistUiEvent
    data object OnCloseNewPlaylist : AddToPlaylistUiEvent
    data class OnNewPlaylistNameChanged(val name: String) : AddToPlaylistUiEvent
    data object OnCreatePlaylist : AddToPlaylistUiEvent
}
