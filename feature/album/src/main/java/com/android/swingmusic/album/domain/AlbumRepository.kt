package com.android.swingmusic.album.domain

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.AlbumWithInfo
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    suspend fun getAlbumCount(): Flow<Resource<Int>>

    suspend fun getPagingAlbums(sortBy: String, sortOrder: Int): Flow<PagingData<Album>>

    suspend fun getAlbumWithInfo(albumHash: String): Flow<Resource<AlbumWithInfo>>

    suspend fun addAlbumToFavorite(albumHash: String): Flow<Resource<Boolean>>

    suspend fun removeAlbumFromFavorite(albumHash: String): Flow<Resource<Boolean>>
}
