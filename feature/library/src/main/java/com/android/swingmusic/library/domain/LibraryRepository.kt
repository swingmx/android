package com.android.swingmusic.library.domain


/** Counts are null when they couldn't be loaded, so the row just shows no count. */
data class LibraryCounts(
    val playlists: Int? = null,
    val albums: Int? = null,
    val artists: Int? = null,
)

interface LibraryRepository {
    suspend fun getBaseUrl(): String

    suspend fun getCounts(): LibraryCounts
}
