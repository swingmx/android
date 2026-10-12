package com.android.swingmusic.playlist.data.repository

import com.android.swingmusic.auth.data.baseurlholder.BaseUrlHolder
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.mapper.Map.toPlaylist
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.network.data.api.service.NetworkApiService
import com.android.swingmusic.network.data.dto.AddToPlaylistRequestDto
import com.android.swingmusic.network.data.dto.SaveAsPlaylistRequestDto
import com.android.swingmusic.playlist.domain.PlaylistRepository
import com.android.swingmusic.playlist.domain.PlaylistTarget
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class DataPlaylistRepository @Inject constructor(
    private val networkApiService: NetworkApiService,
    private val authRepository: AuthRepository
) : PlaylistRepository {

    override suspend fun getAllPlaylists(): Resource<List<Playlist>> = request("Couldn't load playlists") {
        networkApiService.getAllPlaylists(
            url = "${baseUrl()}playlists",
            bearerToken = bearerToken()
        ).data.orEmpty().map { it.toPlaylist() }
    }

    override suspend fun addToPlaylist(playlistId: String, item: PlaylistTarget): Resource<Unit> =
        request("Couldn't add to playlist", conflictMessage = "Already in this playlist") {
            networkApiService.addToPlaylist(
                url = "${baseUrl()}playlists/$playlistId/add",
                request = AddToPlaylistRequestDto(itemType = item.type.value, itemHash = item.hash),
                bearerToken = bearerToken()
            )
            Unit
        }

    override suspend fun saveAsPlaylist(name: String, item: PlaylistTarget): Resource<Unit> =
        request(
            "Couldn't create playlist",
            conflictMessage = "A playlist with that name already exists"
        ) {
            networkApiService.saveAsPlaylist(
                url = "${baseUrl()}playlists/save-item",
                request = SaveAsPlaylistRequestDto(
                    itemType = item.type.value,
                    itemHash = item.hash,
                    playlistName = name
                ),
                bearerToken = bearerToken()
            )
            Unit
        }

    private suspend fun baseUrl() = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl()

    private suspend fun bearerToken() =
        "Bearer ${AuthTokenHolder.accessToken ?: authRepository.getAccessToken()}"

    private suspend fun <T> request(
        errorMessage: String,
        conflictMessage: String = errorMessage,
        block: suspend () -> T
    ): Resource<T> =
        try {
            Resource.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val message = when (e.code()) {
                409 -> conflictMessage
                else -> errorMessage
            }
            Resource.Error(message = message)
        } catch (e: Exception) {
            Timber.tag("PlaylistRepository").e(e)
            Resource.Error(message = errorMessage)
        }
}
