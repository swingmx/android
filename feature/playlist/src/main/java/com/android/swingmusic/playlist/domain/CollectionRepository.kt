package com.android.swingmusic.playlist.domain

import com.android.swingmusic.core.data.util.Resource

/** A user-made collection of albums, artists, mixes and playlists. */
data class Collection(
    val id: Int,
    val name: String,
    val description: String,
    val itemCount: Int,
)

/** What gets added to a collection: a whole album or artist. */
data class CollectionTarget(
    val type: Type,
    val hash: String,
    val suggestedName: String,
) {
    enum class Type(val value: String) { ALBUM("album"), ARTIST("artist") }
}

interface CollectionRepository {
    suspend fun getCollections(): Resource<List<Collection>>

    suspend fun createCollection(name: String, item: CollectionTarget): Resource<Unit>

    suspend fun addToCollection(collectionId: Int, item: CollectionTarget): Resource<Unit>
}
