package com.android.swingmusic.library.data.repository

import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toAlbum
import com.android.swingmusic.core.data.mapper.Map.toArtist
import com.android.swingmusic.core.data.mapper.Map.toRecentFavourite
import com.android.swingmusic.core.data.mapper.Map.toTrack
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.library.domain.FavouriteType
import com.android.swingmusic.library.domain.FavouritesPage
import com.android.swingmusic.library.domain.FavouritesRepository
import com.android.swingmusic.library.domain.FavouritesSummary
import com.android.swingmusic.network.data.api.service.NetworkApiService
import com.android.swingmusic.network.data.dto.ToggleFavoriteRequest
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject

class DataFavouritesRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : FavouritesRepository {

    override suspend fun getSummary(
        trackLimit: Int,
        albumLimit: Int,
        artistLimit: Int
    ): Resource<FavouritesSummary> = request("Couldn't load favourites") {
        val dto = networkApiService.getFavourites(
            url = "${baseUrl()}favorites",
            bearerToken = authRepository.requireBearerToken(),
            trackLimit = trackLimit,
            albumLimit = albumLimit,
            artistLimit = artistLimit
        )
        val tracks = dto.tracks.orEmpty().map { it.toTrack() }
        val albums = dto.albums.orEmpty().map { it.toAlbum() }
        val artists = dto.artists.orEmpty().map { it.toArtist() }
        FavouritesSummary(
            tracks = tracks,
            albums = albums,
            artists = artists,
            trackCount = dto.count?.tracks ?: tracks.size,
            albumCount = dto.count?.albums ?: albums.size,
            artistCount = dto.count?.artists ?: artists.size,
            recents = dto.recents.orEmpty().mapNotNull { it.toRecentFavourite() }
        )
    }

    override suspend fun getTracks(start: Int, limit: Int): Resource<FavouritesPage<Track>> =
        request("Couldn't load favourite tracks") {
            val dto = networkApiService.getFavouriteTracks("${baseUrl()}favorites/tracks", authRepository.requireBearerToken(), start, limit)
            FavouritesPage(dto.tracks.orEmpty().map { it.toTrack() }, dto.total?.takeIf { it >= 0 })
        }

    override suspend fun getAlbums(start: Int, limit: Int): Resource<FavouritesPage<Album>> =
        request("Couldn't load favourite albums") {
            val dto = networkApiService.getFavouriteAlbums("${baseUrl()}favorites/albums", authRepository.requireBearerToken(), start, limit)
            FavouritesPage(dto.albums.orEmpty().map { it.toAlbum() }, dto.total?.takeIf { it >= 0 })
        }

    override suspend fun getArtists(start: Int, limit: Int): Resource<FavouritesPage<Artist>> =
        request("Couldn't load favourite artists") {
            val dto = networkApiService.getFavouriteArtists("${baseUrl()}favorites/artists", authRepository.requireBearerToken(), start, limit)
            FavouritesPage(dto.artists.orEmpty().map { it.toArtist() }, dto.total?.takeIf { it >= 0 })
        }

    override suspend fun setFavourite(hash: String, type: FavouriteType, isFavourite: Boolean): Resource<Unit> =
        request("Couldn't update favourites") {
            val body = ToggleFavoriteRequest(hash = hash, type = type.value)
            if (isFavourite) {
                networkApiService.addFavorite("${baseUrl()}favorites/add", body, authRepository.requireBearerToken())
            } else {
                networkApiService.removeFavorite("${baseUrl()}favorites/remove", body, authRepository.requireBearerToken())
            }
            Unit
        }

    private suspend fun baseUrl() = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl() ?: ""


    private suspend fun <T> request(errorMessage: String, block: suspend () -> T): Resource<T> = try {
        Resource.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.tag("FavouritesRepository").e(e)
        Resource.Error(message = errorMessage)
    }
}
