package com.android.swingmusic.auth.data.mapper

import com.android.swingmusic.auth.data.dto.UserDto
import com.android.swingmusic.auth.data.dto.LogInResultDto
import com.android.swingmusic.auth.domain.model.LogInResult
import com.android.swingmusic.database.domain.model.User

internal fun LogInResultDto.toModel(): LogInResult {
    return LogInResult(
        accessToken = accessToken ?: "",
        maxAge = maxAge ?: 0,
        msg = msg ?: "",
        refreshToken = refreshToken ?: ""
    )
}

internal fun UserDto.toModel(): User {
    return User(
        email = email ?: "",
        firstname = firstname ?: "",
        id = id ?: 0,
        image = image ?: "",
        lastname = lastname ?: "",
        roles = roles ?: emptyList<String>(),
        username = username ?: ""
    )
}
