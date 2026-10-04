package com.android.swingmusic.home.data.repository

import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toHomeSections
import com.android.swingmusic.core.data.mapper.Map.toTrack
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.HomeSection
import com.android.swingmusic.core.domain.model.Mix
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.home.domain.HomeRepository
import com.android.swingmusic.network.data.api.service.NetworkApiService
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class DataHomeRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : HomeRepository {

    override suspend fun getBaseUrl(): String {
        return BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl() ?: ""
    }

    private suspend fun authHeader(): String {
        val token = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        return "Bearer ${token ?: "TOKEN NOT FOUND"}"
    }

    override suspend fun getHomeSections(): Resource<List<HomeSection>> {
        return try {
            val sections = networkApiService.getHome(
                url = "${getBaseUrl()}nothome/",
                bearerToken = authHeader(),
                limit = HOME_ITEMS_PER_SECTION
            ).toHomeSections()
            Resource.Success(sections)
        } catch (e: IOException) {
            Resource.Error(message = "Check your connection to the server and try again.")
        } catch (e: HttpException) {
            Resource.Error(message = "The server couldn't load your home (${e.code()}).")
        } catch (e: Exception) {
            Timber.tag("HOME").e(e)
            Resource.Error(message = "Something went wrong loading your home.")
        }
    }

    override suspend fun getPlaylistTracks(playlistId: String): Resource<List<Track>> {
        return fetchTracks("playlist") {
            networkApiService.getPlaylistTracks(
                url = "${getBaseUrl()}playlists/$playlistId",
                bearerToken = authHeader(),
                limit = PLAYLIST_TRACK_LIMIT
            ).tracks.orEmpty().map { it.toTrack() }
        }
    }

    override suspend fun getMixTracks(mix: Mix): Resource<List<Track>> {
        return fetchTracks("mix") {
            networkApiService.getMixTracks(
                url = "${getBaseUrl()}plugins/mixes/",
                bearerToken = authHeader(),
                mixId = mix.id,
                sourceHash = mix.sourceHash,
                ogSourceHash = mix.ogSourceHash
            ).tracks.orEmpty().map { it.toTrack() }
        }
    }

    private suspend fun fetchTracks(
        what: String,
        fetch: suspend () -> List<Track>
    ): Resource<List<Track>> {
        return try {
            val tracks = fetch()
            if (tracks.isEmpty()) Resource.Error(message = "This $what has no tracks to play.")
            else Resource.Success(tracks)
        } catch (e: IOException) {
            Resource.Error(message = "Couldn't reach the server to play this $what.")
        } catch (e: Exception) {
            Timber.tag("HOME").e(e)
            Resource.Error(message = "Couldn't play this $what.")
        }
    }

    private companion object {
        const val HOME_ITEMS_PER_SECTION = 12
        const val PLAYLIST_TRACK_LIMIT = 500
    }
}
