package com.android.swingmusic.folder.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.mapper.Map.toTrack
import com.android.swingmusic.core.domain.model.Folder
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
            val startIndex = nextPageNumber * params.loadSize

            val requestDto = FoldersAndTracksRequestDto(
                folder = folderPath,
                tracksOnly = false,
                limit = params.loadSize,
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
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}