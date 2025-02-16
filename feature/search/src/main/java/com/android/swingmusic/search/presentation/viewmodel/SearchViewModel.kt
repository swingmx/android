package com.android.swingmusic.search.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.search.domain.reposotory.SearchRepository
import com.android.swingmusic.search.presentation.event.SearchUiEvent
import com.android.swingmusic.search.presentation.state.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _searchParams = MutableStateFlow("")

    private fun updateSearchState(update: SearchState.() -> SearchState) {
        _searchState.update { it.update() }
    }

    init {
        viewModelScope.launch {
            _searchParams
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotBlank()) {
                        performSearch(query)
                    }
                }
        }
    }

    private fun performSearch(searchParams: String) {
        viewModelScope.launch {
            combine(
                searchRepository.getTopSearchResults(searchParams),
                searchRepository.searchTracks(searchParams),
                searchRepository.searchAlbums(searchParams),
                searchRepository.searchArtists(searchParams)
            ) { topResults, tracks, albums, artists ->
                updateSearchState {
                    copy(
                        isLoading = listOf(
                            topResults,
                            tracks,
                            albums,
                            artists
                        ).any { it is Resource.Loading },
                        isError = listOf(
                            topResults,
                            tracks,
                            albums,
                            artists
                        ).any { it is Resource.Error },
                        errorMessage = listOfNotNull(
                            (topResults as? Resource.Error)?.message,
                            (tracks as? Resource.Error)?.message,
                            (albums as? Resource.Error)?.message,
                            (artists as? Resource.Error)?.message
                        ).firstOrNull(),
                        topSearchResults = (topResults as? Resource.Success)?.data,
                        tracksSearchResult = (tracks as? Resource.Success)?.data,
                        albumsSearchResult = (albums as? Resource.Success)?.data,
                        artistsSearchResult = (artists as? Resource.Success)?.data
                    )
                }
            }.collectLatest {}
        }
    }


    fun onSearchUiEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.OnSearchParamChanged -> {
                _searchParams.value = event.searchParams.trim()
                updateSearchState { copy(searchParams = event.searchParams) }
            }

            is SearchUiEvent.OnRetrySearch -> {
                _searchState.value.searchParams.let {
                    if (it.isNotBlank()) {
                        performSearch(it)
                    }
                }
            }
        }
    }
}
