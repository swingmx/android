package com.android.swingmusic.playlist.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.Playlist

@Immutable
internal data class AddToPlaylistUiState(
    val isLoadingPlaylists: Boolean = true,
    val errorLoadingPlaylists: String? = null,
    val playlists: List<Playlist> = emptyList(),

    val showNewPlaylistField: Boolean = false,
    val newPlaylistName: String = "",
    val newPlaylistError: String? = null,

    /** True while an add or create request is running, so taps are ignored. */
    val isSaving: Boolean = false,
    val errorAddingToPlaylist: String? = null,
)
