package com.android.swingmusic.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.android.swingmusic.database.data.entity.QueueEntity

@Dao
interface QueueDao {

    @Transaction
    suspend fun insertQueueInTransaction(tracks: List<QueueEntity>) {
        clearQueue()
        insertQueue(tracks)
    }

    @Query("DELETE FROM queue")
    suspend fun clearQueue()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(tracks: List<QueueEntity>)

    @Query("SELECT * FROM queue")
    suspend fun getSavedQueue(): List<QueueEntity>
}
