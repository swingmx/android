package com.android.swingmusic.library.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist

/** Favourite albums or artists in a grid; [kind] picks which. */
@Immutable
internal data class FavouriteCardsUiState(
    val kind: FavouriteCardsKind = FavouriteCardsKind.ALBUMS,
    val baseUrl: String = "",
    val isLoadingItems: Boolean = true,
    val errorLoadingItems: String? = null,
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val total: Int? = null,
    val isLoadingMoreItems: Boolean = false,
    val endReached: Boolean = false,
) {
    val itemCount: Int get() = if (kind == FavouriteCardsKind.ALBUMS) albums.size else artists.size
}

enum class FavouriteCardsKind(val title: String, val noun: String) {
    ALBUMS("Favourite Albums", "album"),
    ARTISTS("Favourite Artists", "artist"),
}
