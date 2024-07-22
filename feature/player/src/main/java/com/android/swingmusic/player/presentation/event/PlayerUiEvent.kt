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

    object OnToggleShuffleMode : PlayerUiEvent

    object OnClickMore : PlayerUiEvent

    object OnRetry : PlayerUiEvent
}
