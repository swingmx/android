package com.android.swingmusic.search.data.repository

import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toAlbumsSearchResult
import com.android.swingmusic.core.data.mapper.Map.toArtistsSearchResult
import com.android.swingmusic.core.data.mapper.Map.toTopSearchResults
import com.android.swingmusic.core.data.mapper.Map.toTracksSearchResult
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AlbumsSearchResult
import com.android.swingmusic.core.domain.model.ArtistsSearchResult
import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.core.domain.model.TracksSearchResult
import com.android.swingmusic.network.data.api.service.NetworkApiService
import com.android.swingmusic.search.domain.reposotory.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DataSearchRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : SearchRepository {

    override suspend fun searchAlbums(searchParams: String): Flow<Resource<AlbumsSearchResult>> {
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()

        return flow {
            try {
                emit(Resource.Loading())

                val albumsSearchResultDto = networkApiService.searchAlbums(
                    url = "${baseUrl}search/",
                    bearerToken = "Bearer $accessToken",
                    searchParams = searchParams
                )
                val albumsSearchResult = albumsSearchResultDto.toAlbumsSearchResult()

                emit(Resource.Success(data = albumsSearchResult))

            } catch (e: Exception) {
                emit(Resource.Error(message = "Error Searching"))
            }
        }
    }

    override suspend fun searchArtists(searchParams: String): Flow<Resource<ArtistsSearchResult>> {
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()

        return flow {
            try {
                emit(Resource.Loading())

                val artistsSearchResultDto = networkApiService.searchArtists(
                    url = "${baseUrl}search/",
                    bearerToken = "Bearer $accessToken",
                    searchParams = searchParams
                )
                val artistsSearchResult = artistsSearchResultDto.toArtistsSearchResult()

                emit(Resource.Success(data = artistsSearchResult))

            } catch (e: Exception) {
                emit(Resource.Error(message = "Error Searching"))
            }
        }
    }

    override suspend fun searchTracks(searchParams: String): Flow<Resource<TracksSearchResult>> {
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()

        return flow {
            try {
                emit(Resource.Loading())

                val tracksSearchResultDto = networkApiService.searchTracks(
                    url = "${baseUrl}search/",
                    bearerToken = "Bearer $accessToken",
                    searchParams = searchParams
                )
                val tracksSearchResult = tracksSearchResultDto.toTracksSearchResult()

                emit(Resource.Success(data = tracksSearchResult))

            } catch (e: Exception) {
                emit(Resource.Error(message = "Error Searching"))
            }
        }
    }

    override suspend fun getTopSearchResults(searchParams: String): Flow<Resource<TopSearchResults>> {
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()

        return flow {
            try {
                emit(Resource.Loading())

                val topSearchResultsDto = networkApiService.getTopSearchResults(
                    url = "${baseUrl}search/top",
                    bearerToken = "Bearer $accessToken",
                    searchParams = searchParams
                )
                val topSearchResult = topSearchResultsDto.toTopSearchResults()

                emit(Resource.Success(data = topSearchResult))

            } catch (e: Exception) {
                emit(Resource.Error(message = "Error Searching"))
            }
        }
    }
}
