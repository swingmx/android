package com.android.swingmusic.library.data.repository

import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toHomeSections
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.HomeItem
import com.android.swingmusic.library.domain.LibraryCounts
import com.android.swingmusic.library.domain.LibraryRepository
import com.android.swingmusic.network.data.api.service.NetworkApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

class DataLibraryRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : LibraryRepository {

    override suspend fun getBaseUrl(): String = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl() ?: ""

    override suspend fun getCounts(): LibraryCounts = coroutineScope {
        val baseUrl = getBaseUrl()
        val token = bearerToken()
        val playlists = async {
            countOrNull { networkApiService.getAllPlaylists("${baseUrl}playlists", token).data?.size }
        }
        val albums = async {
            countOrNull { networkApiService.getAlbumsCount("${baseUrl}getall/albums", token).total }
        }
        val artists = async {
            countOrNull { networkApiService.getArtistsCount("${baseUrl}getall/artists", token).total }
        }
        LibraryCounts(playlists = playlists.await(), albums = albums.await(), artists = artists.await())
    }

    override suspend fun getRecentlyAddedAlbums(): Resource<List<Album>> = try {
        val sections = networkApiService.getHome(
            url = "${getBaseUrl()}nothome/",
            bearerToken = bearerToken(),
            limit = RECENTLY_ADDED_LIMIT
        ).toHomeSections()
        val albums = sections.firstOrNull { it.key == RECENTLY_ADDED_KEY }
            ?.items
            ?.filterIsInstance<HomeItem.AlbumItem>()
            ?.map { it.album }
            .orEmpty()
        Resource.Success(albums)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.tag("LibraryRepository").e(e)
        Resource.Error(message = "Couldn't load recently added")
    }

    private suspend fun bearerToken() =
        "Bearer ${AuthTokenHolder.accessToken ?: authRepository.getAccessToken()}"

    private suspend fun countOrNull(block: suspend () -> Int?): Int? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.tag("LibraryRepository").e(e)
        null
    }

    private companion object {
        const val RECENTLY_ADDED_KEY = "recently_added"
        const val RECENTLY_ADDED_LIMIT = 12
    }
}
