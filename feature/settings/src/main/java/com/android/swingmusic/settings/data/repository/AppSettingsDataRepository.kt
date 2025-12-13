package com.android.swingmusic.settings.data.repository

import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.core.domain.util.SortOrder
import com.android.swingmusic.settings.data.datastore.AppSettings
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsDataRepository @Inject constructor(
    private val appSettings: AppSettings
) : AppSettingsRepository {

    // --- Album Settings ---
    override val albumGridCount: Flow<Int> = appSettings.getAlbumGridCount

    override val albumSortBy: Flow<SortBy> = appSettings.getAlbumSortBy.map {
        SortBy.valueOf(it)
    }

    override val albumSortOrder: Flow<SortOrder> = appSettings.getAlbumSortOrder.map {
        SortOrder.valueOf(it)
    }

    override suspend fun setAlbumGridCount(count: Int) {
        appSettings.updateAlbumGridCount(count)
    }

    override suspend fun setAlbumSortBy(sortBy: SortBy) {
        appSettings.updateAlbumSortBy(sortBy.name)
    }

    override suspend fun setAlbumSortOrder(order: SortOrder) {
        appSettings.updateAlbumSortOrder(order.name)
    }

    // --- Artist Settings ---
    override val artistGridCount: Flow<Int> = appSettings.getArtistGridCount

    override val artistSortBy: Flow<SortBy> = appSettings.getArtistSortBy.map {
        SortBy.valueOf(it)
    }

    override val artistSortOrder: Flow<SortOrder> = appSettings.getArtistSortOrder.map {
        SortOrder.valueOf(it)
    }

    override suspend fun setArtistGridCount(count: Int) {
        appSettings.updateArtistGridCount(count)
    }

    override suspend fun setArtistSortBy(sortBy: SortBy) {
        appSettings.updateArtistSortBy(sortBy.name)
    }

    override suspend fun setArtistSortOrder(order: SortOrder) {
        appSettings.updateArtistSortOrder(order.name)
    }
}
