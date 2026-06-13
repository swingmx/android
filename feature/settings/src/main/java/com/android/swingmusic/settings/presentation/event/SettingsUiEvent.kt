package com.android.swingmusic.settings.presentation.event

internal sealed interface SettingsUiEvent {
    data object OnBackPressed : SettingsUiEvent
    data object OnClickRePairDevice : SettingsUiEvent
    data class OnToggleUseLyricsPlugin(val enabled: Boolean) : SettingsUiEvent
    data class OnToggleLyricsAutoDownload(val enabled: Boolean) : SettingsUiEvent
    data class OnToggleLyricsOverrideUnsynced(val enabled: Boolean) : SettingsUiEvent
}
