package com.android.swingmusic.playlist.domain

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.core.domain.model.Track

/** A playlist's info with one page of its tracks. */
data class PlaylistPage(val playlist: Playlist, val tracks: List<Track>)

interface PlaylistRepository {
    suspend fun getBaseUrl(): String

    suspend fun getAllPlaylists(): Resource<List<Playlist>>

    suspend fun getPlaylist(playlistId: String, start: Int, limit: Int): Resource<PlaylistPage>

    suspend fun createPlaylist(name: String): Resource<Unit>

    suspend fun addToPlaylist(playlistId: String, item: PlaylistTarget): Resource<Unit>

    suspend fun saveAsPlaylist(name: String, item: PlaylistTarget): Resource<Unit>

    suspend fun renamePlaylist(playlist: Playlist, name: String): Resource<Unit>

    /** [imageUri] is a content URI from the system picker. */
    suspend fun setPlaylistImage(playlist: Playlist, imageUri: String): Resource<Unit>

    suspend fun removePlaylistImage(playlistId: String): Resource<Unit>

    suspend fun togglePin(playlistId: String): Resource<Unit>

    suspend fun deletePlaylist(playlistId: String): Resource<Unit>

    /** [index] is the track's position in the whole playlist. */
    suspend fun removeTrack(playlistId: String, trackHash: String, index: Int): Resource<Unit>
}
