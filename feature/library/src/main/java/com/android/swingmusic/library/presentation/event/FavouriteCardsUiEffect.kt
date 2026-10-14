package com.android.swingmusic.library.presentation.event

internal sealed class FavouriteCardsUiEffect {
    data object NavigateBack : FavouriteCardsUiEffect()
    data class NavigateToAlbum(val albumHash: String) : FavouriteCardsUiEffect()
    data class NavigateToArtist(val artistHash: String) : FavouriteCardsUiEffect()
}
