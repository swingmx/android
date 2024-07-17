package com.android.swingmusic.network.domain.repository

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Flow<Resource<FoldersAndTracks>>

    fun getPagingArtists(sortBy: String, sortOrder: Int): Flow<PagingData<Artist>>

    suspend fun getArtistsCount(): Flow<Resource<Int>>

    suspend fun logLastPlayedTrackToServer(track: Track, playDuration: Int, source: String)
}
