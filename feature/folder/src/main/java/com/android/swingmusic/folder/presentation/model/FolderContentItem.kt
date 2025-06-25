package com.android.swingmusic.folder.presentation.model

import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.Track

sealed class FolderContentItem {
    data class FolderItem(val folder: Folder) : FolderContentItem()
    data class TrackItem(val track: Track) : FolderContentItem()
}