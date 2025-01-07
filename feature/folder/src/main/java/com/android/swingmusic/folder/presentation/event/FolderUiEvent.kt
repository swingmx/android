package com.android.swingmusic.folder.presentation.event

import com.android.swingmusic.core.domain.model.Folder

interface FolderUiEvent {
    data class OnClickFolder(val folder: Folder) : FolderUiEvent

    data class OnClickNavPath(val folder: Folder) : FolderUiEvent

    data class OnBackNav(val folder: Folder) : FolderUiEvent

    data class OnRetry(val event: FolderUiEvent) : FolderUiEvent

    data class ToggleTrackFavorite(val trackHash: String, val isFavorite: Boolean) : FolderUiEvent
}
