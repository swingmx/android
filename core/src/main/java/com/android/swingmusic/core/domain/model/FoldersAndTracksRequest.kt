package com.android.swingmusic.core.domain.model

data class FoldersAndTracksRequest(
    // path
    val folder: String,
    val tracksOnly: Boolean
)
