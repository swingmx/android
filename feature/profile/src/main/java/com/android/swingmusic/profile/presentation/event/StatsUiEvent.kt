package com.android.swingmusic.profile.presentation.event

import com.android.swingmusic.profile.presentation.state.StatsOrder
import com.android.swingmusic.profile.presentation.state.StatsPeriod

internal sealed interface StatsUiEvent {
    data object OnBackClicked : StatsUiEvent
    data object OnRetry : StatsUiEvent
    data object OnRefresh : StatsUiEvent
    data class OnPeriodSelected(val period: StatsPeriod) : StatsUiEvent
    data class OnOrderSelected(val order: StatsOrder) : StatsUiEvent
    data class OnTrackClicked(val index: Int) : StatsUiEvent
    data class OnArtistClicked(val artistHash: String) : StatsUiEvent
    data class OnAlbumClicked(val albumHash: String) : StatsUiEvent
}
