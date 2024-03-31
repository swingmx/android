package com.android.swingmusic.network.domain.repository

import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.util.Resource

interface NetworkRepository {
    suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Resource<FoldersAndTracks>

}
