package com.android.swingmusic.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.swingmusic.database.data.entity.LastPlayedTrackEntity

@Dao
interface LastPlayedTrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastPlayedTrack(lastPlayedTrack: LastPlayedTrackEntity)

    @Query("SELECT * FROM last_played_track LIMIT 1")
    suspend fun getLastPlayedTrack(): LastPlayedTrackEntity?
}
