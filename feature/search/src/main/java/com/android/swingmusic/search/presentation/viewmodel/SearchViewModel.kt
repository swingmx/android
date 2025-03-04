package com.android.swingmusic.search.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.search.domain.reposotory.SearchRepository
import com.android.swingmusic.search.presentation.event.SearchUiEvent
import com.android.swingmusic.search.presentation.state.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
                        searchTopResults(query)
                    }
                }
        }
    }

    private fun searchTopResults(params: String) {
        viewModelScope.launch {
            searchRepository.getTopSearchResults(params).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        updateSearchState {
                            copy(
                                isLoading = true,
                                hasSearched = true,
                                topItemTracks = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        updateSearchState {
                            copy(
                                isError = true,
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }

                    is Resource.Success -> {
                        val topResultItem = result.data?.topResultItem
                        val tracks = result.data?.tracks ?: emptyList()
                        val albums = result.data?.albums ?: emptyList()
                        val artists = result.data?.artists ?: emptyList()

                        updateSearchState {
                            copy(
                                isError = false,
                                isLoading = false,
                                topSearchResults = TopSearchResults(
                                    topResultItem = topResultItem,
                                    tracks = tracks,
                                    albums = albums,
                                    artists = artists
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getTopItemTracks(event: SearchUiEvent) {
        viewModelScope.launch {
            val request = when (event) {
                is SearchUiEvent.OnGetArtistTracks -> {
                    searchRepository.getArtistTracks(event.artistHash)
                }

                is SearchUiEvent.OnGetAlbumTacks -> {
                    searchRepository.getAlbumTracks(event.albumHash)
                }

                else -> null
            }

            request?.let { flow ->
                flow.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            updateSearchState { copy(isLoadingTopItemTracks = true) }
                        }

                        is Resource.Success -> {
                            val tracks = resource.data

                            updateSearchState {
                                copy(
                                    isLoadingTopItemTracks = false,
                                    topItemTracks = tracks
                                )
                            }
                        }

                        is Resource.Error -> {
                            updateSearchState {
                                copy(isLoadingTopItemTracks = false)
                            }
                        }
                    }
                }
            }
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
                        searchTopResults(it)
                    }
                }
            }

            is SearchUiEvent.OnGetArtistTracks -> {
                getTopItemTracks(event)
            }

            is SearchUiEvent.OnGetAlbumTacks -> {
                getTopItemTracks(event)
            }
        }
    }
}
