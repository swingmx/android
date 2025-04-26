package com.android.swingmusic.search.presentation.event

sealed interface SearchUiEvent {

    data class OnSearchParamChanged(val searchParams: String) : SearchUiEvent

    data class OnGetArtistTracks(val artistHash: String) : SearchUiEvent

    data class OnGetAlbumTacks(val albumHash: String) : SearchUiEvent

    data class OnSearchAllTacks(val searchParams: String) : SearchUiEvent

    data class OnSearchAllAlbums(val searchParams: String) : SearchUiEvent

    data class OnSearchAllArtists(val searchParams: String) : SearchUiEvent

    data object OnClearSearchAllResources : SearchUiEvent

    data object OnRetrySearchTopResults : SearchUiEvent

    data class OnToggleTrackFavorite(val trackHash: String, val isFavorite: Boolean) : SearchUiEvent

}
