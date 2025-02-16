package com.android.swingmusic.search.domain.reposotory

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AlbumsSearchResult
import com.android.swingmusic.core.domain.model.ArtistsSearchResult
import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.core.domain.model.TracksSearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    suspend fun searchAlbums(searchParams: String): Flow<Resource<AlbumsSearchResult>>

    suspend fun searchArtists(searchParams: String): Flow<Resource<ArtistsSearchResult>>

    suspend fun searchTracks(searchParams: String): Flow<Resource<TracksSearchResult>>

    suspend fun getTopSearchResults(searchParams: String): Flow<Resource<TopSearchResults>>


}
