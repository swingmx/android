package com.android.swingmusic.library.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.library.domain.LibraryCounts

@Immutable
internal data class LibraryUiState(
    val avatarPath: String? = null,
    val baseUrl: String = "",
    val counts: LibraryCounts = LibraryCounts(),
    val isLoadingRecentlyAdded: Boolean = true,
    val recentlyAdded: List<Album> = emptyList(),
)
