package com.android.swingmusic.settings.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val albumGridCount: Flow<Int>
    val artistGridCount: Flow<Int>
    
    suspend fun setAlbumGridCount(count: Int)
    suspend fun setArtistGridCount(count: Int)
}
