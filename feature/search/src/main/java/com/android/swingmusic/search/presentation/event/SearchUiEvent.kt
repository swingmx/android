package com.android.swingmusic.search.presentation.event

sealed interface SearchUiEvent {

    data class OnSearchParamChanged(val searchParams: String) : SearchUiEvent

    data object OnRetrySearch : SearchUiEvent

}
