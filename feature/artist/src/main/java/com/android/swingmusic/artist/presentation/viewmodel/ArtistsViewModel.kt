package com.android.swingmusic.artist.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.android.swingmusic.artist.domain.repository.ArtistRepository
import com.android.swingmusic.artist.presentation.event.ArtistUiEvent
import com.android.swingmusic.artist.presentation.state.ArtistsUiState
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.core.domain.util.SortOrder
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {
    private var baseUrl: MutableState<String?> = mutableStateOf(null)
    private val _artistsUiState: MutableState<ArtistsUiState> = mutableStateOf(ArtistsUiState())
    val artistsUiState: State<ArtistsUiState> = _artistsUiState

    val sortArtistsByEntries: List<Pair<SortBy, String>> = listOf(
        Pair(SortBy.LAST_PLAYED, "lastplayed"),
        Pair(SortBy.CREATED_DATE, "created_date"),
        Pair(SortBy.PLAY_COUNT, "playcount"),
        Pair(SortBy.PLAY_DURATION, "playduration"),
        Pair(SortBy.NO_OF_TRACKS, "trackcount"),
        Pair(SortBy.NO_OF_ALBUMS, "albumcount"),
        Pair(SortBy.NAME, "name"),
        Pair(SortBy.DURATION, "duration"),
    )

    init {
        settingsRepository.artistGridCount.onEach { gridCount ->
            _artistsUiState.value = _artistsUiState.value.copy(gridCount = gridCount)
        }.launchIn(viewModelScope)

        combine(
            settingsRepository.artistSortOrder.distinctUntilChanged(),
            settingsRepository.artistSortBy.distinctUntilChanged()
        ) { sortOrder, sortBy ->
            val sortByPair = sortArtistsByEntries.find { it.first == sortBy }
                ?: Pair(SortBy.LAST_PLAYED, "lastplayed")

            Pair(sortOrder, sortByPair)
        }.onEach { (sortOrder, sortByPair) ->
            _artistsUiState.value = _artistsUiState.value.copy(
                sortOrder = sortOrder,
                sortBy = sortByPair
            )
            getPagingArtists(
                sortBy = _artistsUiState.value.sortBy.second,
                sortOrder = _artistsUiState.value.sortOrder
            )
        }.launchIn(viewModelScope)
    }

    init {
        getBaseUrl()
        getArtistsCount()
    }

    fun baseUrl() = baseUrl

    private fun getBaseUrl() {
        viewModelScope.launch {
            baseUrl.value = authRepository.getBaseUrl()
        }
    }

    private fun getArtistsCount() {
        viewModelScope.launch {
            artistRepository.getArtistsCount().collectLatest {
                _artistsUiState.value = _artistsUiState.value.copy(totalArtists = it)
            }
        }
    }

    private fun getPagingArtists(sortBy: String, sortOrder: SortOrder) {
        val order = when (sortOrder) {
            SortOrder.DESCENDING -> 1
            SortOrder.ASCENDING -> 0
        }
        viewModelScope.launch {
            _artistsUiState.value = _artistsUiState.value.copy(
                pagingArtists = artistRepository.getPagingArtists(
                    sortBy = sortBy,
                    sortOrder = order
                ).cachedIn(viewModelScope)
            )
        }
    }

    private fun updateGridCount(count: Int) {
        viewModelScope.launch {
            settingsRepository.setArtistGridCount(count)
        }
    }

    fun onArtistUiEvent(event: ArtistUiEvent) {
        when (event) {
            is ArtistUiEvent.OnSortBy -> {
                viewModelScope.launch {
                    // Retry fetching artist count if the previous sorting resulted to Error
                    if (_artistsUiState.value.totalArtists is Resource.Error) {
                        getArtistsCount()
                    }

                    if (event.sortByPair == _artistsUiState.value.sortBy) {
                        val newOrder = if (_artistsUiState.value.sortOrder == SortOrder.ASCENDING)
                            SortOrder.DESCENDING else SortOrder.ASCENDING

                        settingsRepository.setArtistSortOrder(newOrder)
                    } else {
                        settingsRepository.setArtistSortOrder(SortOrder.DESCENDING)
                        settingsRepository.setArtistSortBy(event.sortByPair.first)
                    }
                }
            }

            is ArtistUiEvent.OnClickArtist -> {
                // TODO: Navigate from the UI (handled by UI navigator)
            }

            is ArtistUiEvent.OnUpdateGridCount -> {
                updateGridCount(event.newCount)
            }

            is ArtistUiEvent.OnRetry -> {
                if (_artistsUiState.value.totalArtists is Resource.Error) {
                    getArtistsCount()
                }
            }

            is ArtistUiEvent.OnPullToRefresh -> {
                val sortBy = _artistsUiState.value.sortBy
                val sortOrder = _artistsUiState.value.sortOrder

                getPagingArtists(
                    sortBy = sortBy.second,
                    sortOrder = sortOrder
                )
            }
        }
    }
}
