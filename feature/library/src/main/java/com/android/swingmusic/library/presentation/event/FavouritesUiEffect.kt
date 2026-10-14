package com.android.swingmusic.library.presentation.event

import com.android.swingmusic.core.domain.model.Track

internal sealed class FavouritesUiEffect {
    data object NavigateToProfile : FavouritesUiEffect()
    data object NavigateToTracks : FavouritesUiEffect()
    data object NavigateToAlbums : FavouritesUiEffect()
    data object NavigateToArtists : FavouritesUiEffect()
    data class NavigateToAlbum(val albumHash: String) : FavouritesUiEffect()
    data class NavigateToArtist(val artistHash: String) : FavouritesUiEffect()
    data class PlayTracks(val tracks: List<Track>, val startIndex: Int, val shuffle: Boolean) : FavouritesUiEffect()
    data class ShowSnackbar(val message: String, val undoable: Boolean = false) : FavouritesUiEffect()
}
