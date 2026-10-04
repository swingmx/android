package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class SettingsUiState(
    val appVersion: String = "",
    val imageCacheBytes: Long? = null,
)
