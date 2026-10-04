package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class LyricsSettingsUiState(
    val usePlugin: Boolean = false,
    val autoDownload: Boolean = false,
    val preferSynced: Boolean = false,
)
