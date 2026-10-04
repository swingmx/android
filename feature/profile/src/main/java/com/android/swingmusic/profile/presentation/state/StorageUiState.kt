package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class StorageUiState(
    val imageCacheBytes: Long? = null,
    val isClearing: Boolean = false,
)
