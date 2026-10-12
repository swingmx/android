package com.android.swingmusic.core.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val customImage: String?,
    val images: List<String>,
    val trackCount: Int,
    val pinned: Boolean = false,
    val duration: Int = 0,
    val settings: PlaylistSettings = PlaylistSettings(),
)

/** Sent back in full on every update, since the server rewrites whatever is missing. */
data class PlaylistSettings(
    val hasGif: Boolean = false,
    val bannerPos: Int = 50,
    val squareImg: Boolean = false,
    val pinned: Boolean = false,
)
