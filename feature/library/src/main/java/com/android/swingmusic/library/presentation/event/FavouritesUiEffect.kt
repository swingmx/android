package com.android.swingmusic.library.presentation.event

internal sealed class FavouritesUiEffect {
    data object NavigateToProfile : FavouritesUiEffect()
}
