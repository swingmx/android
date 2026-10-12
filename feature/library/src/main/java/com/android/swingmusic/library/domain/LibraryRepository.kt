package com.android.swingmusic.library.domain

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album

/** Counts are null when they couldn't be loaded, so the row just shows no count. */
data class LibraryCounts(
    val playlists: Int? = null,
    val albums: Int? = null,
    val artists: Int? = null,
)

interface LibraryRepository {
    suspend fun getBaseUrl(): String

    suspend fun getCounts(): LibraryCounts

    suspend fun getRecentlyAddedAlbums(): Resource<List<Album>>
}
