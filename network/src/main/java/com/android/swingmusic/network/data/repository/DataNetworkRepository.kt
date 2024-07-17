package com.android.swingmusic.network.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toAllArtists
import com.android.swingmusic.core.data.mapper.Map.toFolderAndTracks
import com.android.swingmusic.core.data.mapper.Map.toFoldersAndTracksRequestDto
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.network.data.api.paging.ArtistsSource
import com.android.swingmusic.network.data.api.service.NetworkApiService
import com.android.swingmusic.network.domain.model.LogTrackRequest
import com.android.swingmusic.network.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.sql.Timestamp
import java.time.Instant
import javax.inject.Inject

class DataNetworkRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : NetworkRepository {
    // TODO: Split Network Repository. Handle module related methods in the respective module
    override suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Flow<Resource<FoldersAndTracks>> {

        return flow {
            try {
                emit(Resource.Loading())

                val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
                val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

                val foldersAndTracksDto =
                    networkApiService.getFoldersAndTracks(
                        requestData = requestData.toFoldersAndTracksRequestDto(),
                        baseUrl = "${baseUrl}folder",
                        bearerToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                    )

                emit(Resource.Success(data = foldersAndTracksDto.toFolderAndTracks()))

            } catch (e: IOException) {
                emit(
                    Resource.Error(
                        message = "Unable to fetch folders\nCheck your connection and try again!"
                    )
                )
            } catch (e: HttpException) {
                emit(
                    Resource.Error(
                        message = "Unable to fetch folders\nCheck your connection and try again!"
                    )
                )
            } catch (e: Exception) {
                emit(Resource.Error(message = "Connection Failed"))
            }
        }
    }

    override fun getPagingArtists(sortBy: String, sortOrder: Int): Flow<PagingData<Artist>> {
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 20),
            pagingSourceFactory = {
                ArtistsSource(
                    baseUrl = "${baseUrl}getall/artists",
                    accessToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}",
                    api = networkApiService,
                    sortBy = sortBy,
                    sortOrder = sortOrder
                )
            }
        ).flow
    }

    override suspend fun getArtistsCount(): Flow<Resource<Int>> {
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()

        return flow {
            try {
                emit(Resource.Loading<Int>())

                emit(
                    Resource.Success(
                        data = networkApiService.getSampleArtist(
                            baseUrl = "${baseUrl}getall/artists",
                            bearerToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                        ).toAllArtists().total
                    )
                )
            } catch (e: Exception) {
                emit(Resource.Error(message = "Error loading artists"))
            }
        }
    }

    override suspend fun logLastPlayedTrackToServer(
        track: Track,
        playDuration: Int,
        source: String
    ) {
        try {
            val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
            val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

            val timeStamp = Timestamp.from(Instant.now()).toInstant().epochSecond
            val logRequest = LogTrackRequest(
                trackhash = track.trackHash,
                duration = playDuration,
                timestamp = timeStamp,
                source = source
            )
            networkApiService.logLastPlayedTrackToServer(
                logTrackRequest = logRequest,
                baseUrl = "${baseUrl}logger/track/log",
                bearerToken = "Bearer $accessToken"
            )

        } catch (e: HttpException) {
            Timber.e("NETWORK ERROR LOGGING TRACK TO SERVER")
        } catch (e: Exception) {
            Timber.e("ERROR LOGGING TRACK TO SERVER: CAUSED BY -> ${e.message}")
        }
    }
}
