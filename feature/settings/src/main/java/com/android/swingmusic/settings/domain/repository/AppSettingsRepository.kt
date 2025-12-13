package com.android.swingmusic.settings.domain.repository

import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.core.domain.util.SortOrder
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val albumGridCount: Flow<Int>
    val albumSortBy: Flow<SortBy>
    val albumSortOrder: Flow<SortOrder>

    val artistGridCount: Flow<Int>
    val artistSortBy: Flow<SortBy>
    val artistSortOrder: Flow<SortOrder>

    suspend fun setAlbumGridCount(count: Int)
    suspend fun setAlbumSortBy(sortBy: SortBy)
    suspend fun setAlbumSortOrder(order: SortOrder)

    suspend fun setArtistGridCount(count: Int)
    suspend fun setArtistSortBy(sortBy: SortBy)
    suspend fun setArtistSortOrder(order: SortOrder)
}
