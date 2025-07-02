package com.android.swingmusic.folder.presentation.state

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.folder.presentation.model.FolderContentItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class FoldersAndTracksState(
    val foldersAndTracks: FoldersAndTracks = FoldersAndTracks(emptyList(), emptyList()),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

data class FoldersWithPagingTracksState(
    val folders: Resource<List<Folder>> = Resource.Loading(),
    val pagingTracks: Flow<PagingData<Track>> = emptyFlow()
)

data class FoldersContentPagingState(
    val pagingContent: Flow<PagingData<FolderContentItem>> = emptyFlow()
)
