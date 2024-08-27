package com.android.swingmusic.artist.presentation.event

import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.util.SortBy

interface ArtistUiEvent {
    data class OnSortBy(val sortByPair: Pair<SortBy, String>) : ArtistUiEvent

    data class OnClickArtist(val artistHash: String) : ArtistUiEvent

    data class OnUpdateGridCount(val newCount: Int) : ArtistUiEvent

    object OnRetry : ArtistUiEvent
}
