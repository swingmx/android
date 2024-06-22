package com.android.swingmusic.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "base_url")
data class BaseUrlEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val url: String
)
