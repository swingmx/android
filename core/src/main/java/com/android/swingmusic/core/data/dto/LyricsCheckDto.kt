package com.android.swingmusic.core.data.dto

import com.google.gson.annotations.SerializedName

data class LyricsCheckDto(
    @SerializedName("exists")
    val exists: Boolean?
)
