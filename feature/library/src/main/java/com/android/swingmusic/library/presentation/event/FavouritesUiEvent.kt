package com.android.swingmusic.library.presentation.event

internal sealed interface FavouritesUiEvent {
    data object OnProfileClicked : FavouritesUiEvent
}
