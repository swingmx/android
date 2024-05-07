package com.android.swingmusic.folder.presentation.state

import com.android.swingmusic.core.domain.model.FoldersAndTracks

data class FoldersAndTracksState(
    val foldersAndTracks: FoldersAndTracks = FoldersAndTracks(emptyList(), emptyList()),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)
