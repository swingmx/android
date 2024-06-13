package com.android.swingmusic.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_played_track")
data class LastPlayedTrackEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val trackHash: String,
    val indexInQueue: Int,
    val lastPlayPositionMs: Long
)
