package com.android.swingmusic.player.presentation.event

interface PlayerUiEvent {

    data class OnSeekPlayBack(val value: Float) : PlayerUiEvent

    object OnPrev : PlayerUiEvent

    object OnTogglePlayerState : PlayerUiEvent

    object OnNext : PlayerUiEvent

    object OnResumePlaybackFromError : PlayerUiEvent

    data class OnToggleFavorite(val isFavorite: Boolean, val trackHash: String) : PlayerUiEvent

    object OnClickLyricsIcon : PlayerUiEvent

    object OnToggleRepeatMode : PlayerUiEvent

    data class OnToggleShuffleMode(val toggleShuffle: Boolean = false) : PlayerUiEvent

    object OnClickMore : PlayerUiEvent

    object OnRetry : PlayerUiEvent
}
