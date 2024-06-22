package com.android.swingmusic.database.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("logged_in_user")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val firstname: String,
    val lastname: String,
    val email: String,
    val image: String,
    val roles: List<String>,
    val username: String
)
