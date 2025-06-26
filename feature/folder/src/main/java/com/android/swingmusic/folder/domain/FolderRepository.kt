package com.android.swingmusic.folder.domain

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.RootDirs
import com.android.swingmusic.folder.presentation.model.FolderContentItem
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Flow<Resource<FoldersAndTracks>>
    
    suspend fun getPagingTracks(folderPath: String): Flow<PagingData<Track>>
    
    suspend fun getFolders(folderPath: String): Flow<Resource<List<Folder>>>
    
    suspend fun getPagingContent(folderPath: String): Flow<PagingData<FolderContentItem>>
    
    suspend fun getRootDirectories(): Flow<Resource<RootDirs>>
}
