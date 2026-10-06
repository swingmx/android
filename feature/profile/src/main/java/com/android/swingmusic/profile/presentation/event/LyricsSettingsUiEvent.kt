package com.android.swingmusic.profile.presentation.event

internal sealed interface LyricsSettingsUiEvent {
    data object OnBackClicked : LyricsSettingsUiEvent
    data class OnUsePluginChange(val enabled: Boolean) : LyricsSettingsUiEvent
    data class OnAutoDownloadChange(val enabled: Boolean) : LyricsSettingsUiEvent
    data class OnPreferSyncedChange(val enabled: Boolean) : LyricsSettingsUiEvent
    data class OnWordSweepChange(val enabled: Boolean) : LyricsSettingsUiEvent
}
