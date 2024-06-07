package com.android.swingmusic.database.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.swingmusic.database.data.converter.Converters
import com.android.swingmusic.database.data.dao.LastPlayedTrackDao
import com.android.swingmusic.database.data.dao.QueueDao
import com.android.swingmusic.database.data.entity.LastPlayedTrackEntity
import com.android.swingmusic.database.data.entity.QueueEntity

@Database(
    entities = [QueueEntity::class, LastPlayedTrackEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun queueDao(): QueueDao
    abstract fun lastPlayedTrackDao(): LastPlayedTrackDao
}
