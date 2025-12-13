package com.android.swingmusic.settings.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettings @Inject constructor(
    private val context: Context
) {
    private companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

        private val ALBUM_GRID_COUNT = intPreferencesKey("album_grid_count")
        private val ARTIST_GRID_COUNT = intPreferencesKey("artist_grid_count")
    }

    val getAlbumGridCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ALBUM_GRID_COUNT] ?: 2
    }
    val getArtistGridCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ARTIST_GRID_COUNT] ?: 2
    }

    suspend fun updateAlbumGridCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[ALBUM_GRID_COUNT] = count
        }
    }

    suspend fun updateArtistGridCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[ARTIST_GRID_COUNT] = count
        }
    }
}
