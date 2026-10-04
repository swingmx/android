package com.android.swingmusic.home.presentation.event

import com.android.swingmusic.core.domain.model.HomeItem

internal sealed interface HomeUiEvent {
    data object OnRefresh : HomeUiEvent
    data object OnRetry : HomeUiEvent
    data object OnScreenResumed : HomeUiEvent
    data object OnProfileClicked : HomeUiEvent
    data class OnItemClicked(val item: HomeItem) : HomeUiEvent
}
