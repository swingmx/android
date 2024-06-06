package com.android.swingmusic.network.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.swingmusic.core.data.mapper.Map.toAllArtists
import com.android.swingmusic.core.data.mapper.Map.toFolderAndTracks
import com.android.swingmusic.core.data.mapper.Map.toFoldersAndTracksRequestDto
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.network.data.api.paging.ArtistsSource
import com.android.swingmusic.network.data.api.service.ApiService
import com.android.swingmusic.network.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DataNetworkRepository @Inject constructor(
    private val apiService: ApiService,
) : NetworkRepository {
    override suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Resource<FoldersAndTracks> {
        return try {
            Resource.Loading<FoldersAndTracks>()
            val token = ""
            val foldersAndTracksDto =
                apiService.getFoldersAndTracks(
                    requestData = requestData.toFoldersAndTracksRequestDto(),
                    bearerToken = "Bearer $token"
                )
            Resource.Success(data = foldersAndTracksDto.toFolderAndTracks())
        } catch (e: IOException) {
            Resource.Error(
                message = e.localizedMessage
                    ?: "Unable to fetch folders\nCheck your connection and try again!"
            )

        } catch (e: HttpException) {
            Resource.Error(
                message = e.localizedMessage ?: "An unexpected error occurred!"
            )
        }
    }

    override fun getPagingArtists(sortBy: String, sortOrder: Int): Flow<PagingData<Artist>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 20),
            pagingSourceFactory = {
                ArtistsSource(api = apiService, sortBy = sortBy, sortOrder = sortOrder)
            }
        ).flow
    }

    override suspend fun getArtistsCount(): Resource<Int> {
        return try {
            Resource.Loading<Int>()

            Resource.Success(data = apiService.getSampleArtist().toAllArtists().total)
        } catch (e: Exception) {
            Resource.Error(message = "Error loading artists")
        }
    }
}
