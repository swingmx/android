package com.android.swingmusic.library.presentation.event

import com.android.swingmusic.core.domain.model.Track

internal sealed class FavouriteTracksUiEffect {
    data object NavigateBack : FavouriteTracksUiEffect()
    data class PlayTracks(val tracks: List<Track>, val startIndex: Int, val shuffle: Boolean) : FavouriteTracksUiEffect()
    data class ShowSnackbar(val message: String, val undoable: Boolean = false) : FavouriteTracksUiEffect()
}
