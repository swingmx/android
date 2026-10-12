package com.android.swingmusic.playlist.domain

import com.android.swingmusic.core.domain.model.Track

/** What gets added to a playlist: a whole album or artist, or a list of tracks. */
data class PlaylistTarget(
    val type: Type,
    val hash: String,
    val suggestedName: String,
) {
    enum class Type(val value: String) { ALBUM("album"), ARTIST("artist"), TRACKS("tracks") }
}

fun Track.toPlaylistTarget() = PlaylistTarget(
    type = PlaylistTarget.Type.TRACKS,
    hash = trackHash,
    suggestedName = title
)
