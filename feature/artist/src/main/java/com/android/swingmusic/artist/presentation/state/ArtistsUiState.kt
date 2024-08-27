package com.android.swingmusic.artist.presentation.state

import androidx.paging.PagingData
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.core.domain.util.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class ArtistsUiState(
    val pagingArtists: Flow<PagingData<Artist>> = emptyFlow(),
    val totalArtists: Resource<Int> = Resource.Success(data = 0),
    val sortBy: Pair<SortBy, String> = Pair(SortBy.NO_OF_TRACKS, "trackcount"),
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val gridCount: Int = 2
)
