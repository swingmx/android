package com.android.swingmusic.core.domain.model

data class TopSearchResults(
    val topResultItem: TopResultItem?,
    val tracks: List<Track>,
    val albums: List<Album>,
    val artists: List<Artist>,
)
