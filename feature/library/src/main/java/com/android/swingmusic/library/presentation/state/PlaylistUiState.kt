package com.android.swingmusic.library.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.core.domain.model.Track

@Immutable
internal data class PlaylistUiState(
    val baseUrl: String = "",
    val isLoadingPlaylist: Boolean = true,
    val errorLoadingPlaylist: String? = null,
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoadingMoreTracks: Boolean = false,

    val showOptionsSheet: Boolean = false,
    val showRenameDialog: Boolean = false,
    val renameText: String = "",
    val renameError: String? = null,
    val showDeleteDialog: Boolean = false,
    /** True while an edit is running, so its buttons ignore taps. */
    val isSaving: Boolean = false,
) {
    val hasMoreTracks: Boolean get() = playlist != null && tracks.size < playlist.trackCount
}
