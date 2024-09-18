package com.android.swingmusic.player.domain.repository

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.database.domain.model.LastPlayedTrack
import kotlinx.coroutines.flow.Flow

interface PLayerRepository {

    suspend fun insertQueue(track: List<Track>)

    suspend fun getSavedQueue(): List<Track>

    suspend fun clearQueue()

    suspend fun updateLastPlayedTrack(
        trackHash: String,
        indexInQueue: Int,
        source: QueueSource,
        lastPlayPositionMs: Long
    )

    suspend fun getLastPlayedTrack(): LastPlayedTrack?

    suspend fun logLastPlayedTrackToServer(track: Track, playDuration: Int, source: String)

    suspend fun addTrackToFavorite(trackHash: String): Flow<Resource<Boolean>>

    suspend fun removeTrackFromFavorite(trackHash: String): Flow<Resource<Boolean>>
}
