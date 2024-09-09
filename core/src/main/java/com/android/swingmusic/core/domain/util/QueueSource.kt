package com.android.swingmusic.core.domain.util

interface QueueSource {
    data class ALBUM(val albumHash: String) : QueueSource // TODO: Add disc parameter
    data class ARTIST(val artistHash: String) : QueueSource
    data class FOLDER(val path: String) : QueueSource
    data class PLAYLIST(val id: String) : QueueSource
    data class QUERY(val query: String) : QueueSource
    object FAVORITE : QueueSource
    object UNKNOWN : QueueSource
}
