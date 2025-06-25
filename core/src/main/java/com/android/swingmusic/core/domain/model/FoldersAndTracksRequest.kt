package com.android.swingmusic.core.domain.model

data class FoldersAndTracksRequest(
    // used as path
    val folder: String,
    val tracksOnly: Boolean,
    val limit: Int = -1,
    val start: Int = 0
)
