package com.android.swingmusic.core.domain.model

data class AlbumsAndAppearances(
    val albums: List<Album>,
    val appearances: List<Album>,
    val artistName: String,
    val compilations: List<Album>,
    val singlesAndEps: List<Album>
)
