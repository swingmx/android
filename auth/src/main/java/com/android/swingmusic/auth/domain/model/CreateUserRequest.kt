package com.android.swingmusic.auth.domain.model

data class CreateUserRequest(
    val id: Int = 0,
    val email: String,
    val username: String,
    val password: String,
    val roles: List<String>
)
