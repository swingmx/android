package com.android.swingmusic.auth.data.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("email")
    val email: String?,
    @SerializedName("firstname")
    val firstname: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("lastname")
    val lastname: String?,
    @SerializedName("roles")
    val roles: List<String>?,
    @SerializedName("username")
    val username: String?
)
