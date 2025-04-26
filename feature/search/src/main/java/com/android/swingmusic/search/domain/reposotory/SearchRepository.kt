package com.android.swingmusic.search.domain.reposotory

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AlbumsSearchResult
import com.android.swingmusic.core.domain.model.ArtistsSearchResult
import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TracksSearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    suspend fun getTopSearchResults(searchParams: String): Flow<Resource<TopSearchResults>>

    suspend fun getArtistTracks(artistHash: String): Flow<Resource<List<Track>>>

    suspend fun getAlbumTracks(albumHash: String): Flow<Resource<List<Track>>>

    suspend fun searchAllTracks(searchParams: String): Flow<Resource<TracksSearchResult>>

    suspend fun searchAllAlbums(searchParams: String): Flow<Resource<AlbumsSearchResult>>

    suspend fun searchAllArtists(searchParams: String): Flow<Resource<ArtistsSearchResult>>
}
