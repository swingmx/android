package com.android.swingmusic.artist.domain.repository

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    fun getPagingArtists(sortBy: String, sortOrder: Int): Flow<PagingData<Artist>>

    suspend fun getArtistsCount(): Flow<Resource<Int>>
}
