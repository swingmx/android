package com.android.swingmusic.auth.domain.model

data class LogInResult(
    val accessToken: String,
    val maxAge: Long,
    val msg: String,
    val refreshToken: String
)
