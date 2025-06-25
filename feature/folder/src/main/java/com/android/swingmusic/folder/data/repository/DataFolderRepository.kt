package com.android.swingmusic.folder.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.mapper.Map.toModel
import com.android.swingmusic.core.data.mapper.Map.toFoldersAndTracksRequestDto
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.folder.data.paging.FoldersPagingSource
import com.android.swingmusic.folder.domain.FolderRepository
import com.android.swingmusic.folder.presentation.model.FolderContentItem
import com.android.swingmusic.network.data.api.service.NetworkApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DataFolderRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : FolderRepository {
    override suspend fun getFoldersAndTracks(requestData: FoldersAndTracksRequest): Flow<Resource<FoldersAndTracks>> {

        return flow {
            try {
                emit(Resource.Loading())

                val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
                val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

                val foldersAndTracksDto =
                    networkApiService.getFoldersAndTracks(
                        requestData = requestData.toFoldersAndTracksRequestDto(),
                        url = "${baseUrl}folder",
                        bearerToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                    )

                emit(Resource.Success(data = foldersAndTracksDto.toModel()))

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

    override suspend fun getPagingTracks(folderPath: String): Flow<PagingData<Track>> {
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

        /*return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 20),
            pagingSourceFactory = {
                FoldersPagingSource(
                    api = networkApiService,
                    folderPath = folderPath,
                    baseUrl = "${baseUrl}folder",
                    accessToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                )
            }
        ).flow*/

        return emptyFlow()
    }

    override suspend fun getPagingContent(folderPath: String): Flow<PagingData<FolderContentItem>> {
        val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 20),
            pagingSourceFactory = {
                FoldersPagingSource(
                    api = networkApiService,
                    folderPath = folderPath,
                    baseUrl = "${baseUrl}folder",
                    accessToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                )
            }
        ).flow
    }

    override suspend fun getFolders(folderPath: String): Flow<Resource<List<Folder>>> {
        return flow {
            try {
                emit(Resource.Loading())

                val accessToken = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
                val baseUrl = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

                val requestDto = FoldersAndTracksRequestDto(
                    folder = folderPath,
                    tracksOnly = false,
                    limit = 1, // We only need folders, so minimal tracks
                    start = 0
                )

                val foldersAndTracksDto = networkApiService.getFoldersAndTracks(
                    requestData = requestDto,
                    url = "${baseUrl}folder",
                    bearerToken = "Bearer ${accessToken ?: "TOKEN NOT FOUND"}"
                )

                val folders = foldersAndTracksDto.foldersDto?.map { folderDto ->
                    Folder(
                        trackCount = folderDto.fileCount ?: 0,
                        folderCount = folderDto.folderCount ?: 0,
                        isSym = folderDto.isSym ?: false,
                        name = folderDto.name ?: "",
                        path = folderDto.path ?: ""
                    )
                } ?: emptyList()
                emit(Resource.Success(data = folders))

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
}
