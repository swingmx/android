package com.android.swingmusic.search.presentation.state

import com.android.swingmusic.core.domain.model.AlbumsSearchResult
import com.android.swingmusic.core.domain.model.ArtistsSearchResult
import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.core.domain.model.TracksSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

data class SearchState(
    val searchParams: String = "",
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val topSearchResults: TopSearchResults? = null,
    val tracksSearchResult: TracksSearchResult? = null,
    val albumsSearchResult: AlbumsSearchResult? = null,
    val artistsSearchResult: ArtistsSearchResult? = null
)
