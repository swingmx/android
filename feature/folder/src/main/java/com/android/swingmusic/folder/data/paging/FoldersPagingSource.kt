package com.android.swingmusic.folder.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.mapper.Map.toTrack
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.util.CustomPagingException
import com.android.swingmusic.folder.presentation.model.FolderContentItem
import com.android.swingmusic.network.data.api.service.NetworkApiService
import retrofit2.HttpException
import java.io.IOException

class FoldersPagingSource(
    private val api: NetworkApiService,
    private val folderPath: String,
    private val baseUrl: String,
    private val accessToken: String
) : PagingSource<Int, FolderContentItem>() {

    override fun getRefreshKey(state: PagingState<Int, FolderContentItem>): Int? = state.anchorPosition

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FolderContentItem> {
        return try {
            val nextPageNumber = params.key ?: 0
            // Use consistent page size (20) for startIndex calculation instead of variable params.loadSize
            val pageSize = 20
            val startIndex = nextPageNumber * pageSize

            val requestDto = FoldersAndTracksRequestDto(
                folder = folderPath,
                tracksOnly = false,
                limit = pageSize,
                start = startIndex
            )

            val foldersAndTracksDto = api.getFoldersAndTracks(
                url = baseUrl,
                bearerToken = accessToken,
                requestData = requestDto
            )

            // Direct mapping following Albums pattern
            val folderItems = if (nextPageNumber == 0) {
                foldersAndTracksDto.foldersDto?.map { folderDto ->
                    FolderContentItem.FolderItem(
                        Folder(
                            name = folderDto.name ?: "",
                            path = folderDto.path ?: "",
                            isSym = folderDto.isSym ?: false,
                            folderCount = folderDto.folderCount ?: 0,
                            trackCount = folderDto.fileCount ?: 0
                        )
                    )
                } ?: emptyList()
            } else emptyList()
            
            val trackItems = foldersAndTracksDto.tracksDto?.map { trackDto ->
                FolderContentItem.TrackItem(trackDto.toTrack())
            } ?: emptyList()
            
            LoadResult.Page(
                data = folderItems + trackItems,
                prevKey = if (nextPageNumber == 0) null else nextPageNumber - 1,
                nextKey = if (trackItems.isEmpty()) null else nextPageNumber + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(CustomPagingException("Network connection failed. Please check your internet connection.", e))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "Folder not found or has been moved."
                401 -> "Authentication failed. Please log in again."
                403 -> "Access denied to this folder."
                500 -> "Server error. Please try again later."
                else -> "Failed to load folder contents. Please try again."
            }
            LoadResult.Error(CustomPagingException(errorMessage, e))
        } catch (e: Exception) {
            LoadResult.Error(CustomPagingException("An unexpected error occurred while loading folder contents.", e))
        }
    }
}
