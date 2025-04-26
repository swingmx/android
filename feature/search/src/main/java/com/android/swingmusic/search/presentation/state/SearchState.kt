package com.android.swingmusic.search.presentation.state

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.core.domain.model.Track

data class SearchState(
    val searchParams: String = "",
    val viewAllSearchParam: String = "",
    val isError: Boolean = false,
    val isLoadingTopResult: Boolean = false,
    val isLoadingTopItemTracks: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val topSearchResults: TopSearchResults = TopSearchResults(
        topResultItem = null,
        tracks = emptyList(),
        albums = emptyList(),
        artists = emptyList()
    ),
    val topItemTracks: List<Track>? = emptyList(),
    val viewAllTracks: Resource<List<Track>>? = null,
    val viewAllAlbums: Resource<List<Album>>? = null,
    val viewAllArtists: Resource<List<Artist>>? = null
)
