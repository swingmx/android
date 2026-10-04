package com.android.swingmusic.home.domain

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.HomeSection
import com.android.swingmusic.core.domain.model.Mix
import com.android.swingmusic.core.domain.model.Track

interface HomeRepository {
    suspend fun getBaseUrl(): String

    suspend fun getHomeSections(): Resource<List<HomeSection>>

    suspend fun getPlaylistTracks(playlistId: String): Resource<List<Track>>

    suspend fun getMixTracks(mix: Mix): Resource<List<Track>>
}
