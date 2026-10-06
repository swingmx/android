package com.android.swingmusic.profile.domain

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Chart
import com.android.swingmusic.core.domain.model.StatItem
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.database.domain.model.User

interface ProfileRepository {
    suspend fun getBaseUrl(): String

    suspend fun getCachedUser(): User?

    suspend fun fetchUser(): Resource<User>

    suspend fun getWeeklyStats(): Resource<List<StatItem>>

    suspend fun getServerVersion(): String?

    suspend fun getPairCode(): Resource<String>

    /** Results are cached for the app's lifetime; [forceRefresh] skips the cache but still updates it. */
    suspend fun getTopTracks(period: String, orderBy: String, limit: Int, forceRefresh: Boolean = false): Resource<Chart<Track>>

    suspend fun getTopArtists(period: String, orderBy: String, limit: Int, forceRefresh: Boolean = false): Resource<Chart<Artist>>

    suspend fun getTopAlbums(period: String, orderBy: String, limit: Int, forceRefresh: Boolean = false): Resource<Chart<Album>>

    suspend fun triggerScan(fullScan: Boolean): Resource<Unit>

    suspend fun getRootDirectories(): Resource<List<String>>

    suspend fun updateProfile(username: String? = null, password: String? = null): Resource<User>

    suspend fun getImageCacheSize(): Long

    suspend fun clearImageCache()

    suspend fun logOut()
}
