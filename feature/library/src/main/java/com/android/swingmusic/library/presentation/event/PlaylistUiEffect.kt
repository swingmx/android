package com.android.swingmusic.library.presentation.event

import com.android.swingmusic.core.domain.model.Track

internal sealed class PlaylistUiEffect {
    data object NavigateBack : PlaylistUiEffect()
    data object LaunchImagePicker : PlaylistUiEffect()
    data class ShowSnackbar(val message: String) : PlaylistUiEffect()
    data class PlayTracks(val tracks: List<Track>, val startIndex: Int, val shuffle: Boolean) : PlaylistUiEffect()
}
