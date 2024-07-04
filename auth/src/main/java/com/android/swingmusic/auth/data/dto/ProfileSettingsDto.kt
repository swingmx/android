package com.android.swingmusic.auth.data.dto


import com.google.gson.annotations.SerializedName

data class ProfileSettingsDto(
    @SerializedName("enableGuest")
    val enableGuest: Boolean,
    @SerializedName("usersOnLogin")
    val usersOnLogin: Boolean
)
