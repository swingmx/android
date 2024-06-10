package com.android.swingmusic.player.presentation.event

interface PlayerUiEvent {

    data class OnSeekPlayBack(val value: Float): PlayerUiEvent

    object OnPrev: PlayerUiEvent

    object OnTogglePlayerState: PlayerUiEvent

    object OnNext: PlayerUiEvent

    object OnResumePlaybackFromError: PlayerUiEvent

    object OnToggleFavorite: PlayerUiEvent

    object OnClickLyricsIcon: PlayerUiEvent

    object OnToggleRepeatMode: PlayerUiEvent

    object OnToggleShuffleMode: PlayerUiEvent

    object OnClickQueue: PlayerUiEvent

    object OnClickMore: PlayerUiEvent

    object OnRetry: PlayerUiEvent
}
