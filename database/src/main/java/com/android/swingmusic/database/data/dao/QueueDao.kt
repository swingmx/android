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
    suspend fun insertQueue(tracks: List<QueueEntity>)

    @Transaction
    suspend fun insertQueueInTransaction(tracks: List<QueueEntity>) {
        clearQueue()
        insertQueue(tracks)
    }

    @Query("DELETE FROM queue")
    suspend fun clearQueue()

    @Query("SELECT * FROM queue WHERE trackHash = :trackHash")
    suspend fun getTrack(trackHash: String): QueueEntity?

    @Query("SELECT * FROM queue")
    suspend fun getAllTracks(): List<QueueEntity>

}
