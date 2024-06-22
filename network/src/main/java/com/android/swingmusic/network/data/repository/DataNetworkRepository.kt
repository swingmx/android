package com.android.swingmusic.network.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder.accessToken
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
    override suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Resource<FoldersAndTracks> {
        return try {
            Resource.Loading<FoldersAndTracks>()

            val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
            val foldersAndTracksDto =
                networkApiService.getFoldersAndTracks(
                    requestData = requestData.toFoldersAndTracksRequestDto(),
                    bearerToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                )
            Resource.Success(data = foldersAndTracksDto.toFolderAndTracks())
        } catch (e: IOException) {
            Resource.Error(
                message = e.message
                    ?: "Unable to fetch folders\nCheck your connection and try again!"
            )

        } catch (e: HttpException) {
            Resource.Error(
                message = "Unable to fetch folders\nCheck your connection and try again!"
            )
        } catch (e: Exception) {
            Resource.Error(
                message = e.message ?: "An unexpected error occurred!"
            )
        }
    }

    override fun getPagingArtists(sortBy: String, sortOrder: Int): Flow<PagingData<Artist>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 20),
            pagingSourceFactory = {
                ArtistsSource(api = networkApiService, sortBy = sortBy, sortOrder = sortOrder)
            }
        ).flow
    }

    override suspend fun getArtistsCount(): Resource<Int> {
        return try {
            Resource.Loading<Int>()

            Resource.Success(data = networkApiService.getSampleArtist().toAllArtists().total)
        } catch (e: Exception) {
            Resource.Error(message = "Error loading artists")
        }
    }

    override suspend fun logLastPlayedTrackToServer(
        track: Track,
        playDuration: Int,
        source: String
    ) {
        try {
            val timeStamp = Timestamp.from(Instant.now()).toInstant().epochSecond
            val logRequest = LogTrackRequest(
                trackhash = track.trackHash,
                duration = playDuration,
                timestamp = timeStamp,
                source = source
            )
            networkApiService.logLastPlayedTrackToServer(logRequest, "Bearer $accessToken")

        } catch (e: HttpException) {
            Timber.e("ERROR LOGGING TRACK TO SERVER")
        } catch (e: Exception) {
            Timber.e("ERROR LOGGING TRACK TO SERVER: CAUSED BY -> ${e.message}")
        }
    }
}
