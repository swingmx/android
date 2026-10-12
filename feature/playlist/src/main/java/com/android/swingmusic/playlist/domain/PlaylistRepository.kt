package com.android.swingmusic.playlist.domain

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Playlist

interface PlaylistRepository {
    suspend fun getAllPlaylists(): Resource<List<Playlist>>

    suspend fun addToPlaylist(playlistId: String, item: PlaylistTarget): Resource<Unit>

    suspend fun saveAsPlaylist(name: String, item: PlaylistTarget): Resource<Unit>
}
