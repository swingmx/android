package com.android.swingmusic.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.swingmusic.core.domain.util.QueueSource

@Entity(tableName = "last_played_track")
data class LastPlayedTrackEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val trackHash: String,
    val indexInQueue: Int,
    val source: QueueSource,
    val lastPlayPositionMs: Long
)
