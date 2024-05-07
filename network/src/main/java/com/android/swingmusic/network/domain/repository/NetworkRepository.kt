package com.android.swingmusic.network.domain.repository

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AllArtists
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Resource<FoldersAndTracks>

    fun getPagingArtists(sortBy: String, sortOrder: Int): Flow<PagingData<Artist>>

    suspend fun getArtistsCount(): Resource<Int>
}
