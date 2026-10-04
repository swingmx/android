package com.android.swingmusic.profile.presentation.event

internal sealed class LyricsSettingsUiEffect {
    data object NavigateBack : LyricsSettingsUiEffect()
}
