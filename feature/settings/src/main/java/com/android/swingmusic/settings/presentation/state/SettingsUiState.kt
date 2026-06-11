package com.android.swingmusic.settings.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class SettingsUiState(
    val serverUrl: String = "",
    val appVersion: String = "",

    val useLyricsPlugin: Boolean = false,
    val lyricsAutoDownload: Boolean = false,
    val lyricsOverrideUnsynced: Boolean = false,

    val isSigningOut: Boolean = false,
    val signOutError: String? = null
)
