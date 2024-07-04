package com.android.swingmusic.database.domain.model

data class User(
    val email: String,
    val firstname: String,
    val id: Int,
    val image: String,
    val lastname: String,
    val roles: List<String>,
    val username: String
)
