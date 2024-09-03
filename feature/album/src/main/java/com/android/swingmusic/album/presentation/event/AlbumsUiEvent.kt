package com.android.swingmusic.album.presentation.event

import com.android.swingmusic.core.domain.util.SortBy

interface AlbumsUiEvent {
    data class OnSortBy(val sortByPair: Pair<SortBy, String>) : AlbumsUiEvent

    data class OnClickAlbum(val albumHash: String) : AlbumsUiEvent

    data class OnUpdateGridCount(val newCount: Int) : AlbumsUiEvent

    object OnRetry : AlbumsUiEvent
}
