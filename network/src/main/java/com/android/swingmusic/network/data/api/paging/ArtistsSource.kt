package com.android.swingmusic.network.data.api.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.swingmusic.core.data.mapper.Map.toArtist
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.network.data.api.service.ApiService
import retrofit2.HttpException
import java.io.IOException

class ArtistsSource(
    private val api: ApiService,
    private val sortBy: String,
    private val sortOrder: Int
) :
    PagingSource<Int, Artist>() {

    override fun getRefreshKey(state: PagingState<Int, Artist>): Int? = state.anchorPosition

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Artist> {
        return try {
            val nextPageNumber = params.key ?: 0
            val startIndex = nextPageNumber * params.loadSize
            val allArtistsDto = api.getAllArtists(
                startIndex = startIndex,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            LoadResult.Page(
                data = allArtistsDto.artists?.map { it.toArtist() } ?: emptyList(),
                prevKey = if (nextPageNumber == 0) null else nextPageNumber - 1,
                nextKey = if (allArtistsDto.artists?.isEmpty() == true) null else nextPageNumber + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}
