package com.android.swingmusic.auth.data.dto


import com.google.gson.annotations.SerializedName

data class LogInResultDto(
    @SerializedName("accesstoken")
    val accessToken: String?,
    @SerializedName("maxage")
    val maxAge: Long?,
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("refreshtoken")
    val refreshToken: String?
)
