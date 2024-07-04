package com.android.swingmusic.auth.data.mapper

import com.android.swingmusic.auth.data.dto.AllUsersDto
import com.android.swingmusic.auth.data.dto.LogInResultDto
import com.android.swingmusic.auth.data.dto.ProfileSettingsDto
import com.android.swingmusic.auth.data.dto.UserDto
import com.android.swingmusic.auth.domain.model.AllUsers
import com.android.swingmusic.auth.domain.model.LogInResult
import com.android.swingmusic.auth.domain.model.ProfileSettings
import com.android.swingmusic.database.domain.model.User

internal fun ProfileSettingsDto.toModel(): ProfileSettings {
    return ProfileSettings(
        enableGuest = enableGuest,
        usersOnLogin = usersOnLogin
    )
}

internal fun AllUsersDto.toModel(): AllUsers {
    return AllUsers(
        profileSettings = profileSettingsDto.toModel(),
        users = users.map { it.toModel() }
    )
}

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
