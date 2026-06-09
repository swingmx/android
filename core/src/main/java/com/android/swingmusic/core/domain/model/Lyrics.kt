package com.android.swingmusic.core.domain.model

data class Lyrics(
    val synced: Boolean,
    val lines: List<LyricsLine>,
    val copyright: String,
    val exists: Boolean
)
