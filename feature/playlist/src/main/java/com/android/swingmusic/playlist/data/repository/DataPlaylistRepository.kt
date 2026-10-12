package com.android.swingmusic.playlist.data.repository

import org.json.JSONObject
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import com.android.swingmusic.playlist.domain.PlaylistPage
import com.android.swingmusic.network.data.dto.RemovePlaylistTracksRequestDto
import com.android.swingmusic.network.data.dto.PlaylistTrackRefDto
import com.android.swingmusic.network.data.dto.CreatePlaylistRequestDto
import com.android.swingmusic.core.data.mapper.Map.toTrack
import android.net.Uri
import android.content.Context
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
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : PlaylistRepository {

    override suspend fun getBaseUrl(): String = baseUrl()

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

    override suspend fun getPlaylist(playlistId: String, start: Int, limit: Int): Resource<PlaylistPage> =
        request("Couldn't load playlist") {
            val dto = networkApiService.getPlaylist(
                url = "${baseUrl()}playlists/$playlistId",
                bearerToken = bearerToken(),
                start = start,
                limit = limit
            )
            PlaylistPage(
                playlist = dto.info?.toPlaylist() ?: error("Playlist info missing"),
                tracks = dto.tracks.orEmpty().map { it.toTrack() }
            )
        }

    override suspend fun createPlaylist(name: String): Resource<Unit> = request(
        "Couldn't create playlist",
        conflictMessage = "A playlist with that name already exists"
    ) {
        networkApiService.createPlaylist(
            url = "${baseUrl()}playlists/new",
            request = CreatePlaylistRequestDto(name = name),
            bearerToken = bearerToken()
        )
        Unit
    }

    override suspend fun renamePlaylist(playlist: Playlist, name: String): Resource<Unit> =
        request("Couldn't rename playlist") { update(playlist, name = name, image = null) }

    override suspend fun setPlaylistImage(playlist: Playlist, imageUri: String): Resource<Unit> =
        request("Couldn't change the image") {
            val uri = Uri.parse(imageUri)
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } ?: error("Couldn't read the image")
            val part = MultipartBody.Part.createFormData(
                "image",
                "playlist-image",
                bytes.toRequestBody(mimeType.toMediaType())
            )
            update(playlist, name = playlist.name, image = part)
        }

    override suspend fun removePlaylistImage(playlistId: String): Resource<Unit> =
        request("Couldn't remove the image") {
            networkApiService.removePlaylistImage("${baseUrl()}playlists/$playlistId/remove-img", bearerToken())
            Unit
        }

    override suspend fun togglePin(playlistId: String): Resource<Unit> =
        request("Couldn't update the playlist") {
            networkApiService.pinUnpinPlaylist("${baseUrl()}playlists/$playlistId/pin_unpin", bearerToken())
            Unit
        }

    override suspend fun deletePlaylist(playlistId: String): Resource<Unit> =
        request("Couldn't delete playlist") {
            networkApiService.deletePlaylist("${baseUrl()}playlists/$playlistId/delete", bearerToken())
            Unit
        }

    override suspend fun removeTrack(playlistId: String, trackHash: String, index: Int): Resource<Unit> =
        request("Couldn't remove the track") {
            networkApiService.removePlaylistTracks(
                url = "${baseUrl()}playlists/$playlistId/remove-tracks",
                request = RemovePlaylistTracksRequestDto(listOf(PlaylistTrackRefDto(trackHash, index))),
                bearerToken = bearerToken()
            )
            Unit
        }

    // The server rewrites any setting that isn't sent, so all of them go with every update
    private suspend fun update(playlist: Playlist, name: String, image: MultipartBody.Part?) {
        val settings = JSONObject()
            .put("has_gif", playlist.settings.hasGif)
            .put("banner_pos", playlist.settings.bannerPos)
            .put("square_img", playlist.settings.squareImg)
            .put("pinned", playlist.settings.pinned)
            .toString()
        val text = "text/plain".toMediaType()
        networkApiService.updatePlaylist(
            url = "${baseUrl()}playlists/${playlist.id}/update",
            bearerToken = bearerToken(),
            name = name.toRequestBody(text),
            settings = settings.toRequestBody(text),
            // The server requires an image part; an empty one, as the web client sends, keeps the current image
            image = image ?: MultipartBody.Part.createFormData(
                "image",
                "",
                ByteArray(0).toRequestBody("application/octet-stream".toMediaType())
            )
        )
    }

    private suspend fun baseUrl() = BaseUrlHolder.baseUrl ?: authRepository.getBaseUrl() ?: ""

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
