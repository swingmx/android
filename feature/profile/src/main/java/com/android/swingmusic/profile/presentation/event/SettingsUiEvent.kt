package com.android.swingmusic.profile.presentation.event

internal sealed interface SettingsUiEvent {
    data object OnBackClicked : SettingsUiEvent
    data object OnAccountClicked : SettingsUiEvent
    data object OnLyricsClicked : SettingsUiEvent
    data object OnStorageClicked : SettingsUiEvent
    data object OnAboutClicked : SettingsUiEvent
    data object OnScreenResumed : SettingsUiEvent
}
