package com.android.swingmusic.library.presentation.event

import com.android.swingmusic.core.domain.model.Track

internal sealed interface FavouriteTracksUiEvent {
    data object OnBackClicked : FavouriteTracksUiEvent
    data object OnRetry : FavouriteTracksUiEvent
    data object OnLoadMoreTracks : FavouriteTracksUiEvent
    data object OnPlayClicked : FavouriteTracksUiEvent
    data object OnShuffleClicked : FavouriteTracksUiEvent
    data class OnTrackClicked(val index: Int) : FavouriteTracksUiEvent
    data class OnUnfavouriteTrack(val track: Track) : FavouriteTracksUiEvent
    data object OnUndoUnfavourite : FavouriteTracksUiEvent
}
