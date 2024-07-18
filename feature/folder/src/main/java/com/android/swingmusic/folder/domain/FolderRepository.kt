package com.android.swingmusic.folder.domain

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Flow<Resource<FoldersAndTracks>>
}
