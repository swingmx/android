package com.android.swingmusic.auth.data.dto

import com.google.gson.annotations.SerializedName

data class AllUsersDto(
    @SerializedName("settings")
    val profileSettingsDto: ProfileSettingsDto,
    @SerializedName("users")
    val users: List<UserDto>
)
