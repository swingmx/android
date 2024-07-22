package com.android.swingmusic.folder.data.repository

import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toModel
import com.android.swingmusic.core.data.mapper.Map.toFoldersAndTracksRequestDto
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.folder.domain.FolderRepository
import com.android.swingmusic.network.data.api.service.NetworkApiService
import kotlinx.coroutines.flow.Flow
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

                emit(Resource.Success(data = foldersAndTracksDto.toModel().sort()))

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

    private fun FoldersAndTracks.sort(): FoldersAndTracks {
        val folders = this.folders.sortedBy { it.name }
        val tracks = this.tracks.sortedBy { it.title }
        return FoldersAndTracks(folders, tracks)
    }
}
