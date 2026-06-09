package com.android.swingmusic.player.data.repository

import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.dto.LyricsRequestDto
import com.android.swingmusic.core.data.dto.PluginLyricsRequestDto
import com.android.swingmusic.core.data.mapper.Map.toLyrics
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Lyrics
import com.android.swingmusic.network.data.api.service.NetworkApiService
import com.android.swingmusic.player.domain.repository.LyricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class DataLyricsRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : LyricsRepository {

    private suspend fun authHeader(): String {
        val token = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        return "Bearer ${token ?: "TOKEN NOT FOUND"}"
    }

    private suspend fun baseUrl(): String {
        return BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl() ?: ""
    }

    override suspend fun getLyrics(filepath: String, trackHash: String): Flow<Resource<Lyrics>> =
        flow {
            try {
                emit(Resource.Loading())
                val dto = networkApiService.getLyrics(
                    url = "${baseUrl()}lyrics",
                    request = LyricsRequestDto(filepath = filepath, trackhash = trackHash),
                    bearerToken = authHeader()
                )
                emit(Resource.Success(dto.toLyrics()))
            } catch (e: IOException) {
                emit(Resource.Error<Lyrics>(message = "Unable to fetch lyrics\nCheck your connection and try again!"))
            } catch (e: HttpException) {
                emit(Resource.Error<Lyrics>(message = "Unable to fetch lyrics"))
            } catch (e: Exception) {
                Timber.tag("LYRICS").e(e)
                emit(Resource.Error<Lyrics>(message = "Connection Failed"))
            }
        }

    override suspend fun checkLyricsExist(filepath: String, trackHash: String): Boolean {
        return try {
            val dto = networkApiService.checkLyricsExist(
                url = "${baseUrl()}lyrics/check",
                request = LyricsRequestDto(filepath = filepath, trackhash = trackHash),
                bearerToken = authHeader()
            )
            dto.exists ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchLyricsOnline(
        trackHash: String,
        title: String,
        artist: String,
        filepath: String,
        album: String
    ): Flow<Resource<Lyrics>> = flow {
        try {
            emit(Resource.Loading())
            val dto = networkApiService.searchLyricsOnline(
                url = "${baseUrl()}plugins/lyrics/search",
                request = PluginLyricsRequestDto(
                    trackhash = trackHash,
                    title = title,
                    artist = artist,
                    filepath = filepath,
                    album = album
                ),
                bearerToken = authHeader()
            )
            val pluginError = dto.error
            if (!pluginError.isNullOrEmpty()) {
                emit(Resource.Error<Lyrics>(message = pluginError))
            } else {
                val lyrics = dto.toLyrics()
                if (lyrics.lines.isEmpty()) {
                    emit(Resource.Error<Lyrics>(message = "No lyrics found"))
                } else {
                    emit(Resource.Success(lyrics.copy(exists = true)))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error<Lyrics>(message = "Unable to search lyrics\nCheck your connection and try again!"))
        } catch (e: HttpException) {
            emit(Resource.Error<Lyrics>(message = "Unable to search lyrics"))
        } catch (e: Exception) {
            Timber.tag("LYRICS").e(e)
            emit(Resource.Error<Lyrics>(message = "Search failed"))
        }
    }
}
