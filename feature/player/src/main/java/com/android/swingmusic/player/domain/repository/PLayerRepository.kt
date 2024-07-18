package com.android.swingmusic.player.domain.repository

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.database.domain.model.LastPlayedTrack

interface PLayerRepository {

    suspend fun insertQueue(track: List<Track>)

    suspend fun getSavedQueue(): List<Track>

    suspend fun clearQueue()

    suspend fun updateLastPlayedTrack(
        trackHash: String,
        indexInQueue: Int,
        lastPlayPositionMs: Long
    )

    suspend fun getLastPlayedTrack(): LastPlayedTrack?

    suspend fun logLastPlayedTrackToServer(track: Track, playDuration: Int, source: String)
}
