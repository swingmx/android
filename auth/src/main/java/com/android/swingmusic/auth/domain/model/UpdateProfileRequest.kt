package com.android.swingmusic.auth.domain.model

import com.google.gson.annotations.SerializedName

/** Null fields are left out of the request, so the server keeps their current value. */
data class UpdateProfileRequest(
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("password")
    val password: String? = null
)
