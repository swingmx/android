package com.android.swingmusic.artist.domain.repository

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.ArtistInfo
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    suspend fun getArtistsCount(): Flow<Resource<Int>>

    fun getPagingArtists(sortBy: String, sortOrder: Int): Flow<PagingData<Artist>>

    fun getArtistInfo(artistHash: String): Flow<Resource<ArtistInfo>>

    fun getSimilarArtists(artistHash: String): Flow<Resource<List<Artist>>>

    suspend fun addArtistToFavorite(artistHash: String): Flow<Resource<Boolean>>

    suspend fun removeArtistFromFavorite(artistHash: String): Flow<Resource<Boolean>>
}
