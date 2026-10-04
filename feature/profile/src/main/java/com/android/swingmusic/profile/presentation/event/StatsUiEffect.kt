package com.android.swingmusic.profile.presentation.event

import com.android.swingmusic.core.domain.model.Track

internal sealed class StatsUiEffect {
    data object NavigateBack : StatsUiEffect()
    data class PlayTracks(val tracks: List<Track>, val startIndex: Int) : StatsUiEffect()
    data class NavigateToArtist(val artistHash: String) : StatsUiEffect()
    data class NavigateToAlbum(val albumHash: String) : StatsUiEffect()
}
