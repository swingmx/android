package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class LibraryUiState(
    val isAdmin: Boolean = false,

    val isLoadingRootDirs: Boolean = false,
    val errorLoadingRootDirs: String? = null,
    val rootDirs: List<String> = emptyList(),

    val isStartingScan: Boolean = false,
    val showFullScanDialog: Boolean = false,
)
