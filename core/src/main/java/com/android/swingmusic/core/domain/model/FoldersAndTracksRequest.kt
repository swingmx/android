package com.android.swingmusic.core.domain.model

import com.google.gson.annotations.SerializedName

data class FoldersAndTracksRequest(
    // path
    val folder: String,
    val tracksOnly: Boolean
)
