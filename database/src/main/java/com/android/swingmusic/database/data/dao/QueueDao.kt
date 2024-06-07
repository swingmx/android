package com.android.swingmusic.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.android.swingmusic.database.data.entity.QueueEntity

@Dao
interface QueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<QueueEntity>)

    @Transaction
    suspend fun saveTracksInTransaction(tracks: List<QueueEntity>) {
        clearQueue()
        insertTracks(tracks)
    }

    @Query("DELETE FROM queue")
    suspend fun clearQueue()

    @Query("SELECT * FROM queue WHERE trackHash = :trackHash")
    suspend fun getTrack(trackHash: String): QueueEntity?

    @Query("SELECT * FROM queue")
    suspend fun getAllTracks(): List<QueueEntity>

}
