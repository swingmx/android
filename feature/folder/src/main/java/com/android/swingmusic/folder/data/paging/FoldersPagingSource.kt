package com.android.swingmusic.folder.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.mapper.Map.toTrack
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.network.data.api.service.NetworkApiService
import retrofit2.HttpException
import java.io.IOException

class FoldersPagingSource(
    private val api: NetworkApiService,
    private val folderPath: String,
    private val baseUrl: String,
    private val accessToken: String
) : PagingSource<Int, Track>() {

    override fun getRefreshKey(state: PagingState<Int, Track>): Int? = state.anchorPosition

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
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

            val tracks = foldersAndTracksDto.tracksDto?.map { it.toTrack() } ?: emptyList()
            
            LoadResult.Page(
                data = tracks,
                prevKey = if (nextPageNumber == 0) null else nextPageNumber - 1,
                nextKey = if (tracks.isEmpty()) null else nextPageNumber + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}