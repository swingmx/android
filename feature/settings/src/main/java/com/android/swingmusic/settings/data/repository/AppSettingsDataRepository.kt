package com.android.swingmusic.settings.data.repository

import com.android.swingmusic.settings.data.datastore.AppSettings
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsDataRepository @Inject constructor(
    private val appSettings: AppSettings
) : AppSettingsRepository {

    override val albumGridCount: Flow<Int> = appSettings.getAlbumGridCount

    override val artistGridCount: Flow<Int> = appSettings.getArtistGridCount

    override suspend fun setAlbumGridCount(count: Int) {
        appSettings.updateAlbumGridCount(count)
    }

    override suspend fun setArtistGridCount(count: Int) {
        appSettings.updateArtistGridCount(count)
    }
}
