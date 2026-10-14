package com.android.swingmusic.library.presentation.event

internal sealed interface FavouriteCardsUiEvent {
    data object OnScreenResumed : FavouriteCardsUiEvent
    data object OnBackClicked : FavouriteCardsUiEvent
    data object OnRetry : FavouriteCardsUiEvent
    data object OnLoadMoreItems : FavouriteCardsUiEvent
    data class OnItemClicked(val hash: String) : FavouriteCardsUiEvent
}
