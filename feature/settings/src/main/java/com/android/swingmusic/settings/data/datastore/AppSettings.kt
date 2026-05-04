package com.android.swingmusic.settings.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.core.domain.util.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettings @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

        private val ALBUM_GRID_COUNT = intPreferencesKey("album_grid_count")
        private val ALBUM_SORT_BY = stringPreferencesKey("album_sort_by")
        private val ALBUM_SORT_ORDER = stringPreferencesKey("album_sort_order")

        private val ARTIST_GRID_COUNT = intPreferencesKey("artist_grid_count")
        private val ARTIST_SORT_BY = stringPreferencesKey("artist_sort_by")
        private val ARTIST_SORT_ORDER = stringPreferencesKey("artist_sort_order")

        private val USE_LYRICS_PLUGIN = booleanPreferencesKey("use_lyrics_plugin")
        private val LYRICS_AUTO_DOWNLOAD = booleanPreferencesKey("lyrics_auto_download")
        private val LYRICS_OVERRIDE_UNSYNCED = booleanPreferencesKey("lyrics_override_unsynced")
    }

    // Album Flows
    val getAlbumGridCount: Flow<Int> = context.dataStore.data.map {
        it[ALBUM_GRID_COUNT] ?: 2
    }
    val getAlbumSortBy: Flow<String> = context.dataStore.data.map {
        it[ALBUM_SORT_BY] ?: SortBy.LAST_PLAYED.name
    }
    val getAlbumSortOrder: Flow<String> = context.dataStore.data.map {
        it[ALBUM_SORT_ORDER] ?: SortOrder.DESCENDING.name
    }

    // Artist Flows
    val getArtistGridCount: Flow<Int> = context.dataStore.data.map {
        it[ARTIST_GRID_COUNT] ?: 2
    }
    val getArtistSortBy: Flow<String> = context.dataStore.data.map {
        it[ARTIST_SORT_BY] ?: SortBy.LAST_PLAYED.name
    }
    val getArtistSortOrder: Flow<String> = context.dataStore.data.map {
        it[ARTIST_SORT_ORDER] ?: SortOrder.DESCENDING.name
    }

    // Album Update methods
    suspend fun updateAlbumGridCount(count: Int) =
        context.dataStore.edit { it[ALBUM_GRID_COUNT] = count }

    suspend fun updateAlbumSortBy(value: String) =
        context.dataStore.edit { it[ALBUM_SORT_BY] = value }

    suspend fun updateAlbumSortOrder(value: String) =
        context.dataStore.edit { it[ALBUM_SORT_ORDER] = value }

    // Artist Update Methods
    suspend fun updateArtistGridCount(count: Int) =
        context.dataStore.edit { it[ARTIST_GRID_COUNT] = count }

    suspend fun updateArtistSortBy(value: String) =
        context.dataStore.edit { it[ARTIST_SORT_BY] = value }

    suspend fun updateArtistSortOrder(value: String) =
        context.dataStore.edit { it[ARTIST_SORT_ORDER] = value }

    // Lyrics Flows
    val getUseLyricsPlugin: Flow<Boolean> = context.dataStore.data.map {
        it[USE_LYRICS_PLUGIN] ?: false
    }
    val getLyricsAutoDownload: Flow<Boolean> = context.dataStore.data.map {
        it[LYRICS_AUTO_DOWNLOAD] ?: false
    }
    val getLyricsOverrideUnsynced: Flow<Boolean> = context.dataStore.data.map {
        it[LYRICS_OVERRIDE_UNSYNCED] ?: false
    }

    suspend fun updateUseLyricsPlugin(value: Boolean) =
        context.dataStore.edit { it[USE_LYRICS_PLUGIN] = value }

    suspend fun updateLyricsAutoDownload(value: Boolean) =
        context.dataStore.edit { it[LYRICS_AUTO_DOWNLOAD] = value }

    suspend fun updateLyricsOverrideUnsynced(value: Boolean) =
        context.dataStore.edit { it[LYRICS_OVERRIDE_UNSYNCED] = value }
}
