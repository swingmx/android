package com.android.swingmusic.library.presentation.event

import com.android.swingmusic.core.domain.model.Track

internal sealed interface PlaylistUiEvent {
    data object OnBackClicked : PlaylistUiEvent
    data object OnRetry : PlaylistUiEvent
    data object OnLoadMoreTracks : PlaylistUiEvent
    data object OnPlayClicked : PlaylistUiEvent
    data object OnShuffleClicked : PlaylistUiEvent
    data class OnTrackClicked(val index: Int) : PlaylistUiEvent
    data class OnToggleTrackFavourite(val trackHash: String, val isFavourite: Boolean) : PlaylistUiEvent
    data class OnRemoveTrack(val track: Track, val index: Int) : PlaylistUiEvent

    data object OnMoreClicked : PlaylistUiEvent
    data object OnOptionsDismissed : PlaylistUiEvent
    data object OnRenameClicked : PlaylistUiEvent
    data class OnRenameTextChanged(val text: String) : PlaylistUiEvent
    data object OnRenameConfirmed : PlaylistUiEvent
    data object OnRenameDismissed : PlaylistUiEvent
    data object OnChangeImageClicked : PlaylistUiEvent
    data class OnImagePicked(val uri: String) : PlaylistUiEvent
    data object OnRemoveImageClicked : PlaylistUiEvent
    data object OnTogglePinClicked : PlaylistUiEvent
    data object OnDeleteClicked : PlaylistUiEvent
    data object OnDeleteConfirmed : PlaylistUiEvent
    data object OnDeleteDismissed : PlaylistUiEvent
}
