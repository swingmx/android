package com.android.swingmusic.library.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.Track

@Immutable
internal data class FavouriteTracksUiState(
    val baseUrl: String = "",
    val isLoadingTracks: Boolean = true,
    val errorLoadingTracks: String? = null,
    val tracks: List<Track> = emptyList(),
    val total: Int? = null,
    val isLoadingMoreTracks: Boolean = false,
    val endReached: Boolean = false,
)
