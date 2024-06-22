package com.android.swingmusic.auth.data.mapper

import com.android.swingmusic.auth.data.dto.CreateUserResultDto
import com.android.swingmusic.auth.data.dto.LogInResultDto
import com.android.swingmusic.auth.domain.model.CreateUserResult
import com.android.swingmusic.auth.domain.model.LogInResult

internal fun LogInResultDto.toModel(): LogInResult {
    return LogInResult(
        accessToken = accessToken ?: "",
        maxAge = maxAge ?: 0,
        msg = msg ?: "",
        refreshToken = refreshToken ?: ""
    )
}

internal fun CreateUserResultDto.toModel(): CreateUserResult {
    return CreateUserResult(
        email = email ?: "",
        firstname = firstname ?: "",
        id = id ?: 0,
        image = image ?: "",
        lastname = lastname ?: "",
        roles = roles ?: emptyList<String>(),
        username = username ?: ""
    )
}
