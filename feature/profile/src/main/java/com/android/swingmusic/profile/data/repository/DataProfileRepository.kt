package com.android.swingmusic.profile.data.repository

import android.content.Context
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.dto.ChartResponseDto
import com.android.swingmusic.core.data.dto.TriggerScanRequestDto
import com.android.swingmusic.core.data.mapper.Map.toAlbumChart
import com.android.swingmusic.core.data.mapper.Map.toArtistChart
import com.android.swingmusic.core.data.mapper.Map.toRootDirs
import com.android.swingmusic.core.data.mapper.Map.toStatItem
import com.android.swingmusic.core.data.mapper.Map.toTrackChart
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Chart
import com.android.swingmusic.core.domain.model.StatItem
import com.android.swingmusic.database.domain.model.User
import com.android.swingmusic.network.data.api.service.NetworkApiService
import com.android.swingmusic.profile.domain.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@OptIn(ExperimentalCoilApi::class)
class DataProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : ProfileRepository {

    /**
     * Stats only change as you listen, and the server takes seconds per chart,
     * so results are kept until the app process dies. Keys include the server
     * and user, so another account never sees these numbers.
     */
    private val statsCache = ConcurrentHashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> cached(key: String, fetch: suspend () -> Resource<T>): Resource<T> {
        val scopedKey = "${getBaseUrl()}|${getCachedUser()?.id}|$key"
        (statsCache[scopedKey] as? T)?.let { return Resource.Success(it) }

        val result = fetch()
        if (result is Resource.Success) result.data?.let { statsCache[scopedKey] = it }
        return result
    }

    override suspend fun getBaseUrl(): String {
        return BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl() ?: ""
    }

    private suspend fun authHeader(): String {
        val token = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        return "Bearer ${token ?: "TOKEN NOT FOUND"}"
    }

    override suspend fun getCachedUser(): User? = authRepository.getLoggedInUser()

    override suspend fun fetchUser(): Resource<User> = authRepository.fetchCurrentUser()

    override suspend fun getWeeklyStats(): Resource<List<StatItem>> = cached("weekly") {
        try {
            val stats = networkApiService.getWeeklyStats(
                url = "${getBaseUrl()}logger/stats",
                bearerToken = authHeader()
            ).stats.orEmpty().mapNotNull { it.toStatItem() }
            Resource.Success(stats)
        } catch (e: IOException) {
            Resource.Error(message = "Check your connection to the server.")
        } catch (e: HttpException) {
            Resource.Error(message = "The server couldn't load your stats (${e.code()}).")
        } catch (e: Exception) {
            Timber.tag("PROFILE").e(e)
            Resource.Error(message = "Something went wrong loading your stats.")
        }
    }

    override suspend fun getServerVersion(): String? {
        return try {
            networkApiService.getServerSettings(
                url = "${getBaseUrl()}notsettings",
                bearerToken = authHeader()
            ).version?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPairCode(): Resource<String> {
        return try {
            val code = networkApiService.getPairCode(
                url = "${getBaseUrl()}auth/getpaircode",
                bearerToken = authHeader()
            ).code
            if (code.isNullOrBlank()) Resource.Error(message = "The server didn't return a code.")
            else Resource.Success(code)
        } catch (e: IOException) {
            Resource.Error(message = "Check your connection to the server.")
        } catch (e: Exception) {
            Timber.tag("PROFILE").e(e)
            Resource.Error(message = "Couldn't create a pairing code.")
        }
    }

    override suspend fun getTopTracks(period: String, orderBy: String, limit: Int) =
        fetchChart("tracks", period, orderBy, limit) { it.toTrackChart() }

    override suspend fun getTopArtists(period: String, orderBy: String, limit: Int) =
        fetchChart("artists", period, orderBy, limit) { it.toArtistChart() }

    override suspend fun getTopAlbums(period: String, orderBy: String, limit: Int) =
        fetchChart("albums", period, orderBy, limit) { it.toAlbumChart() }

    private suspend fun <T> fetchChart(
        kind: String,
        period: String,
        orderBy: String,
        limit: Int,
        map: (ChartResponseDto) -> Chart<T>
    ): Resource<Chart<T>> = cached("chart:$kind:$period:$orderBy:$limit") {
        try {
            val response = networkApiService.getChart(
                url = "${getBaseUrl()}logger/top-$kind",
                bearerToken = authHeader(),
                duration = period,
                limit = limit,
                orderBy = orderBy
            )
            Resource.Success(map(response))
        } catch (e: IOException) {
            Resource.Error(message = "Check your connection to the server.")
        } catch (e: HttpException) {
            Resource.Error(message = "The server couldn't load your stats (${e.code()}).")
        } catch (e: Exception) {
            Timber.tag("PROFILE").e(e)
            Resource.Error(message = "Something went wrong loading your stats.")
        }
    }

    override suspend fun triggerScan(fullScan: Boolean): Resource<Unit> {
        return try {
            networkApiService.triggerScan(
                url = "${getBaseUrl()}notsettings/trigger-scan",
                bearerToken = authHeader(),
                body = TriggerScanRequestDto(fullScan = fullScan)
            )
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error(message = "Check your connection to the server.")
        } catch (e: Exception) {
            Timber.tag("PROFILE").e(e)
            Resource.Error(message = "Couldn't start the scan.")
        }
    }

    override suspend fun getRootDirectories(): Resource<List<String>> {
        return try {
            val dirs = networkApiService.getRootDirectories(
                url = "${getBaseUrl()}notsettings/get-root-dirs",
                bearerToken = authHeader()
            ).toRootDirs().rootDirs
            Resource.Success(dirs)
        } catch (e: IOException) {
            Resource.Error(message = "Check your connection to the server.")
        } catch (e: Exception) {
            Timber.tag("PROFILE").e(e)
            Resource.Error(message = "Couldn't load root folders.")
        }
    }

    override suspend fun updateProfile(username: String?, password: String?): Resource<User> {
        return authRepository.updateProfile(username = username, password = password)
    }

    override suspend fun getImageCacheSize(): Long = withContext(Dispatchers.IO) {
        context.imageLoader.diskCache?.size ?: 0L
    }

    override suspend fun clearImageCache() = withContext(Dispatchers.IO) {
        context.imageLoader.memoryCache?.clear()
        context.imageLoader.diskCache?.clear()
        Unit
    }

    override suspend fun logOut() {
        statsCache.clear()
        authRepository.logOut()
    }
}
