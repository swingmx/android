package com.android.swingmusic.library.presentation.event

import com.android.swingmusic.core.domain.model.Track

internal sealed interface FavouritesUiEvent {
    data object OnScreenResumed : FavouritesUiEvent
    data object OnProfileClicked : FavouritesUiEvent
    data object OnRetry : FavouritesUiEvent
    data object OnPlayClicked : FavouritesUiEvent
    data object OnShuffleClicked : FavouritesUiEvent
    data class OnTrackClicked(val index: Int) : FavouritesUiEvent
    data class OnUnfavouriteTrack(val track: Track) : FavouritesUiEvent
    data object OnUndoUnfavourite : FavouritesUiEvent
    data object OnSeeAllTracksClicked : FavouritesUiEvent
    data object OnSeeAllAlbumsClicked : FavouritesUiEvent
    data object OnSeeAllArtistsClicked : FavouritesUiEvent
    data class OnAlbumClicked(val albumHash: String) : FavouritesUiEvent
    data class OnArtistClicked(val artistHash: String) : FavouritesUiEvent
}
