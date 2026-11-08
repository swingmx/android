package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.Changelog

@Immutable
internal data class ChangelogUiState(
    val changelog: Changelog? = null,
    val isFirstLaunch: Boolean = false,
    /** The running build's notes haven't been dismissed yet. */
    val isUnseen: Boolean = false,
    val showAllChanges: Boolean = false,
    val expandedVersionCodes: Set<Int> = emptySet(),
)
