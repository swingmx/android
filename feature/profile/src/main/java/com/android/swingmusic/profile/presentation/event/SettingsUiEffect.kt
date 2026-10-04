package com.android.swingmusic.profile.presentation.event

internal sealed class SettingsUiEffect {
    data object NavigateBack : SettingsUiEffect()
    data object NavigateToAccount : SettingsUiEffect()
    data object NavigateToLyrics : SettingsUiEffect()
    data object NavigateToStorage : SettingsUiEffect()
    data object NavigateToAbout : SettingsUiEffect()
}
