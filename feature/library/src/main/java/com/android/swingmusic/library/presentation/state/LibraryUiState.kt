package com.android.swingmusic.library.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.library.domain.LibraryCounts

@Immutable
internal data class LibraryUiState(
    val avatarPath: String? = null,
    val baseUrl: String = "",
    /** True until the first counts arrive; later refreshes keep showing the old ones. */
    val isLoadingCounts: Boolean = true,
    val counts: LibraryCounts = LibraryCounts(),
)
