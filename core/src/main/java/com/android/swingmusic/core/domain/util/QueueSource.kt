package com.android.swingmusic.core.domain.util

interface QueueSource {
    data class ALBUM(val albumHash: String, val name: String) : QueueSource
    data class ARTIST(val artistHash: String, val name: String) : QueueSource
    data class FOLDER(val path: String, val name: String) : QueueSource
    data class PLAYLIST(val id: String, val name: String) : QueueSource
    /** [sourceHash] is needed for the server to place the mix in Recently played. */
    data class MIX(val id: String, val name: String, val sourceHash: String = "") : QueueSource
    object SEARCH : QueueSource
    object FAVORITE : QueueSource
    object UNKNOWN : QueueSource
}
