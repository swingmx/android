package com.android.swingmusic.album.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.swingmusic.album.data.paging.AlbumsPagingSource
import com.android.swingmusic.album.domain.AlbumRepository
import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toAlbumWithInfo
import com.android.swingmusic.core.data.mapper.Map.toAllAlbums
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.AlbumWithInfo
import com.android.swingmusic.network.data.api.service.NetworkApiService
import com.android.swingmusic.network.data.dto.AlbumHashRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DataAlbumRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : AlbumRepository {
    override suspend fun getAlbumCount(): Flow<Resource<Int>> {
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()

        return flow {
            try {
                emit(Resource.Loading<Int>())

                emit(
                    Resource.Success(
                        data = networkApiService.getAlbumsCount(
                            url = "${baseUrl}getall/albums",
                            bearerToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                        ).toAllAlbums().total
                    )
                )
            } catch (e: Exception) {
                emit(Resource.Error(message = "Error loading albums"))
            }
        }
    }

    override suspend fun getPagingAlbums(sortBy: String, sortOrder: Int): Flow<PagingData<Album>> {
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 20),
            pagingSourceFactory = {
                AlbumsPagingSource(
                    baseUrl = "${baseUrl}getall/albums",
                    accessToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}",
                    api = networkApiService,
                    sortBy = sortBy,
                    sortOrder = sortOrder
                )
            }
        ).flow
    }

    override suspend fun getAlbumWithInfo(albumHash: String): Flow<Resource<AlbumWithInfo>> {
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

        return flow {
            try {
                emit(Resource.Loading())

                val albumWithInfo = networkApiService.getAlbumWithInfo(
                    url = "${baseUrl}album",
                    albumHashRequest = AlbumHashRequestDto(albumHash),
                    bearerToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}",
                ).toAlbumWithInfo()

                emit(Resource.Success(data = albumWithInfo))

            } catch (e: Exception) {
                emit(Resource.Error(message = "Error loading album details"))
            }
        }
    }
}
