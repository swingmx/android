package com.android.swingmusic.search.presentation.event

sealed interface SearchUiEvent {

    data class OnSearchParamChanged(val searchParams: String) : SearchUiEvent

    data class OnGetArtistTracks(val artistHash: String) : SearchUiEvent

    data class OnGetAlbumTacks(val albumHash: String) : SearchUiEvent

    data object OnRetrySearch : SearchUiEvent

}
