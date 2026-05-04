package com.android.swingmusic.player.domain.repository

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Lyrics
import kotlinx.coroutines.flow.Flow

interface LyricsRepository {

    suspend fun getLyrics(filepath: String, trackHash: String): Flow<Resource<Lyrics>>

    suspend fun checkLyricsExist(filepath: String, trackHash: String): Boolean

    suspend fun searchLyricsOnline(
        trackHash: String,
        title: String,
        artist: String,
        filepath: String,
        album: String
    ): Flow<Resource<Lyrics>>
}
