package com.android.swingmusic.auth.domain.model

data class CreateUserResult(
    val email: String,
    val firstname: String,
    val id: Int,
    val image: String,
    val lastname: String,
    val roles: List<String>,
    val username: String
)