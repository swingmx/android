package com.android.swingmusic.core.domain.util

interface QueueSource {
    data class ALBUM(val albumHash: String, val name: String) : QueueSource
    data class ARTIST(val artistHash: String, val name: String) : QueueSource
    data class FOLDER(val path: String, val name: String) : QueueSource
    data class PLAYLIST(val id: String, val name: String) : QueueSource
    object SEARCH : QueueSource
    object FAVORITE : QueueSource
    object UNKNOWN : QueueSource
}
