package com.android.swingmusic.home.presentation.event

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.QueueSource

internal sealed class HomeUiEffect {
    data class NavigateToAlbum(val albumHash: String) : HomeUiEffect()
    data class NavigateToArtist(val artistHash: String) : HomeUiEffect()
    data class PlayTracks(val tracks: List<Track>, val source: QueueSource) : HomeUiEffect()
    data class ShowSnackBar(val message: String) : HomeUiEffect()
    data object NavigateToProfile : HomeUiEffect()
}
