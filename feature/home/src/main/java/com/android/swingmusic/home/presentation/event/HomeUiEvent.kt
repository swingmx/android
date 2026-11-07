package com.android.swingmusic.home.presentation.event

import com.android.swingmusic.core.domain.model.HomeItem
import com.android.swingmusic.core.domain.model.Track

internal sealed interface HomeUiEvent {
    data object OnRefresh : HomeUiEvent
    data object OnRetry : HomeUiEvent
    data object OnScreenResumed : HomeUiEvent
    data object OnProfileClicked : HomeUiEvent
    data class OnItemClicked(val item: HomeItem) : HomeUiEvent
    data class OnPlayTrackClicked(val track: Track) : HomeUiEvent
}
