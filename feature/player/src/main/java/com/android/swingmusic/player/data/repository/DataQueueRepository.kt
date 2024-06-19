package com.android.swingmusic.player.data.repository

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.database.data.dao.BaseUrlDao
import com.android.swingmusic.database.data.dao.LastPlayedTrackDao
import com.android.swingmusic.database.data.dao.QueueDao
import com.android.swingmusic.database.data.mapper.toEntity
import com.android.swingmusic.database.data.mapper.toModel
import com.android.swingmusic.database.domain.model.LastPlayedTrack
import com.android.swingmusic.player.domain.repository.QueueRepository
import javax.inject.Inject

class DataQueueRepository @Inject constructor(
    private val queueDao: QueueDao,
    private val lastPlayedTrackDao: LastPlayedTrackDao
) : QueueRepository {

    override suspend fun insertQueue(track: List<Track>) {
        val trackEntities = track.map { it.toEntity() }
        queueDao.insertQueueInTransaction(trackEntities)
    }

    override suspend fun getAllTracks(): List<Track> {
        return queueDao.getAllTracks().map { it.toModel() }
    }

    override suspend fun clearQueue() {
        queueDao.clearQueue()
    }

    override suspend fun updateLastPlayedTrack(
        trackHash: String,
        indexInQueue: Int,
        lastPlayPositionMs: Long
    ) {
        lastPlayedTrackDao.insertLastPlayedTrack(
            LastPlayedTrack(
                trackHash = trackHash,
                indexInQueue = indexInQueue,
                lastPlayPositionMs = lastPlayPositionMs
            ).toEntity()
        )
    }

    override suspend fun getLastPlayedTrack(): LastPlayedTrack? {
        return lastPlayedTrackDao.getLastPlayedTrack()?.toModel()
    }
}
