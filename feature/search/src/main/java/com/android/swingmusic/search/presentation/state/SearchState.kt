package com.android.swingmusic.search.presentation.state

import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.core.domain.model.Track

data class SearchState(
    val searchParams: String = "",
    val isError: Boolean = false,
    val isLoadingTopItemTracks: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val topSearchResults: TopSearchResults = TopSearchResults(
        topResultItem = null,
        tracks = emptyList(),
        albums = emptyList(),
        artists = emptyList()
    ),
    val topItemTracks: List<Track>? = emptyList()
)
