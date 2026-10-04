package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class AboutUiState(
    val appVersion: String = "",
    val serverVersion: String? = null,
)
