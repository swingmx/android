package com.android.swingmusic.artist.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.swingmusic.artist.domain.repository.ArtistRepository
import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toAllArtists
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.artist.data.paging.ArtistsPagingSource
import com.android.swingmusic.network.data.api.service.NetworkApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DataArtistRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : ArtistRepository {

    override suspend fun getArtistsCount(): Flow<Resource<Int>> {
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()

        return flow {
            try {
                emit(Resource.Loading<Int>())

                emit(
                    Resource.Success(
                        data = networkApiService.getArtistsCount(
                            url = "${baseUrl}getall/artists",
                            bearerToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                        ).toAllArtists().total
                    )
                )
            } catch (e: Exception) {
                emit(Resource.Error(message = "Error loading artists"))
            }
        }
    }

    override fun getPagingArtists(sortBy: String, sortOrder: Int): Flow<PagingData<Artist>> {
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 20),
            pagingSourceFactory = {
                ArtistsPagingSource(
                    baseUrl = "${baseUrl}getall/artists",
                    accessToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}",
                    api = networkApiService,
                    sortBy = sortBy,
                    sortOrder = sortOrder
                )
            }
        ).flow
    }
}