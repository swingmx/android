package com.android.swingmusic.folder.presentation.event

import com.android.swingmusic.core.domain.model.Folder

interface FolderUiEvent {
    data class ClickFolder(val folder: Folder) : FolderUiEvent
    data class ClickNavPath(val folder: Folder) : FolderUiEvent
    data class OnBackNav(val folder: Folder) : FolderUiEvent
    object Retry : FolderUiEvent
}
