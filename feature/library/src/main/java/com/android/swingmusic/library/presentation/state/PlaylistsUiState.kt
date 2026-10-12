package com.android.swingmusic.library.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.Playlist

@Immutable
internal data class PlaylistsUiState(
    val baseUrl: String = "",
    val isLoadingPlaylists: Boolean = true,
    val errorLoadingPlaylists: String? = null,
    /** Pinned first, as the web client groups them; each group keeps the server's order. */
    val pinnedPlaylists: List<Playlist> = emptyList(),
    val otherPlaylists: List<Playlist> = emptyList(),

    val showNewPlaylistDialog: Boolean = false,
    val newPlaylistName: String = "",
    val newPlaylistError: String? = null,
    val isCreatingPlaylist: Boolean = false,
)
