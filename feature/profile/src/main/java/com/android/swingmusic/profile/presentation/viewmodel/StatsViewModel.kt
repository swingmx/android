package com.android.swingmusic.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Chart
import com.android.swingmusic.profile.domain.ProfileRepository
import com.android.swingmusic.profile.presentation.event.StatsUiEffect
import com.android.swingmusic.profile.presentation.event.StatsUiEvent
import com.android.swingmusic.profile.presentation.state.ChartState
import com.android.swingmusic.profile.presentation.state.StatsUiState
import com.android.swingmusic.profile.presentation.state.chart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StatsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<StatsUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            val baseUrl = profileRepository.getBaseUrl()
            updateUiState { copy(baseUrl = baseUrl) }
        }
        loadCharts()
    }

    fun onEvent(event: StatsUiEvent) {
        when (event) {
            StatsUiEvent.OnBackClicked -> _uiEffect.trySend(StatsUiEffect.NavigateBack)
            StatsUiEvent.OnRetry -> loadCharts()
            StatsUiEvent.OnRefresh -> refresh()
            is StatsUiEvent.OnPeriodSelected -> {
                if (event.period == _uiState.value.period) return
                updateUiState { copy(period = event.period) }
                loadCharts()
            }

            is StatsUiEvent.OnOrderSelected -> {
                if (event.order == _uiState.value.order) return
                updateUiState { copy(order = event.order) }
                loadCharts()
            }

            is StatsUiEvent.OnTrackClicked -> {
                val tracks = _uiState.value.tracks.chart?.entries?.map { it.item }.orEmpty()
                if (event.index in tracks.indices) {
                    _uiEffect.trySend(StatsUiEffect.PlayTracks(tracks, event.index))
                }
            }

            is StatsUiEvent.OnArtistClicked -> _uiEffect.trySend(StatsUiEffect.NavigateToArtist(event.artistHash))
            is StatsUiEvent.OnAlbumClicked -> _uiEffect.trySend(StatsUiEffect.NavigateToAlbum(event.albumHash))
        }
    }

    private fun loadCharts() {
        val period = _uiState.value.period.apiValue
        val order = _uiState.value.order.apiValue

        updateUiState {
            copy(
                isRefreshing = false,
                tracks = ChartState.Loading,
                artists = ChartState.Loading,
                albums = ChartState.Loading
            )
        }
        loadJob?.cancel()
        // One at a time: parallel requests slow the server down enough to time out.
        loadJob = viewModelScope.launch {
            val tracks = profileRepository.getTopTracks(period, order, TRACK_LIMIT).toChartState()
            updateUiState { copy(tracks = tracks) }

            val artists = profileRepository.getTopArtists(period, order, CARD_LIMIT).toChartState()
            updateUiState { copy(artists = artists) }

            val albums = profileRepository.getTopAlbums(period, order, CARD_LIMIT).toChartState()
            updateUiState { copy(albums = albums) }
        }
    }

    /** Pull to refresh: bypasses the cache and keeps what's on screen if a chart fails. */
    private fun refresh() {
        if (_uiState.value.isRefreshing) return
        val period = _uiState.value.period.apiValue
        val order = _uiState.value.order.apiValue

        updateUiState { copy(isRefreshing = true) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            var failed = false

            val tracks = profileRepository.getTopTracks(period, order, TRACK_LIMIT, forceRefresh = true)
            if (tracks is Resource.Success) updateUiState { copy(tracks = tracks.toChartState()) }
            else failed = true

            val artists = profileRepository.getTopArtists(period, order, CARD_LIMIT, forceRefresh = true)
            if (artists is Resource.Success) updateUiState { copy(artists = artists.toChartState()) }
            else failed = true

            val albums = profileRepository.getTopAlbums(period, order, CARD_LIMIT, forceRefresh = true)
            if (albums is Resource.Success) updateUiState { copy(albums = albums.toChartState()) }
            else failed = true

            updateUiState { copy(isRefreshing = false) }
            if (failed) _uiEffect.trySend(StatsUiEffect.ShowSnackBar("Couldn't refresh all of your stats"))
        }
    }

    private fun <T> Resource<Chart<T>>.toChartState(): ChartState<T> {
        val chart = data
        return if (this is Resource.Success && chart != null) ChartState.Loaded(chart)
        else ChartState.Error(message ?: "Couldn't load this chart.")
    }

    private fun updateUiState(block: StatsUiState.() -> StatsUiState) {
        _uiState.update(block)
    }

    private companion object {
        const val TRACK_LIMIT = 10
        const val CARD_LIMIT = 12
    }
}
