package com.android.swingmusic.profile.presentation.event

internal sealed interface ProfileUiEvent {
    data object OnBackClicked : ProfileUiEvent
    data object OnCopyServerClicked : ProfileUiEvent
    data object OnRetryStats : ProfileUiEvent
    data object OnStatsClicked : ProfileUiEvent
    data object OnLibraryClicked : ProfileUiEvent
    data object OnPairDeviceClicked : ProfileUiEvent
    data object OnSettingsClicked : ProfileUiEvent
    data object OnLogOutClicked : ProfileUiEvent
    data object OnLogOutDismissed : ProfileUiEvent
    data object OnLogOutConfirmed : ProfileUiEvent
}
