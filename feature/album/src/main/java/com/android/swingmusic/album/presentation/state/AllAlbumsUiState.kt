package com.android.swingmusic.album.presentation.state

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.AlbumWithInfo
import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.core.domain.util.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class AllAlbumsUiState(
    val pagingAlbums: Flow<PagingData<Album>> = emptyFlow(),
    val totalAlbums: Resource<Int> = Resource.Success(data = 0),
    val sortBy: Pair<SortBy, String> = Pair(SortBy.DATE, "date"),
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val gridCount: Int = 2
)
