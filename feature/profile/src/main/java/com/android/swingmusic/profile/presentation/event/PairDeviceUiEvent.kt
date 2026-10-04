package com.android.swingmusic.profile.presentation.event

internal sealed interface PairDeviceUiEvent {
    data object OnBackClicked : PairDeviceUiEvent
    data object OnNewCodeClicked : PairDeviceUiEvent
}
