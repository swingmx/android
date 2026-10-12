package com.android.swingmusic.library.domain

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Track

data class FavouritesSummary(
    val tracks: List<Track>,
    val albums: List<Album>,
    val artists: List<Artist>,
    val trackCount: Int,
    val albumCount: Int,
    val artistCount: Int,
)

/** One page of a favourites list. [total] is only known for the first page. */
data class FavouritesPage<T>(val items: List<T>, val total: Int?)

enum class FavouriteType(val value: String) { TRACK("track"), ALBUM("album"), ARTIST("artist") }

interface FavouritesRepository {
    suspend fun getSummary(trackLimit: Int, albumLimit: Int, artistLimit: Int): Resource<FavouritesSummary>

    suspend fun getTracks(start: Int, limit: Int): Resource<FavouritesPage<Track>>

    suspend fun getAlbums(start: Int, limit: Int): Resource<FavouritesPage<Album>>

    suspend fun getArtists(start: Int, limit: Int): Resource<FavouritesPage<Artist>>

    suspend fun setFavourite(hash: String, type: FavouriteType, isFavourite: Boolean): Resource<Unit>
}
