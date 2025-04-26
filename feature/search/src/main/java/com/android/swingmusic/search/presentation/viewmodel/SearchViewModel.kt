package com.android.swingmusic.search.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.player.domain.repository.PLayerRepository
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
    private val searchRepository: SearchRepository,
    private val pLayerRepository: PLayerRepository
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
                                isLoadingTopResult = true,
                                hasSearched = true,
                                topItemTracks = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        updateSearchState {
                            copy(
                                isError = true,
                                isLoadingTopResult = false,
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
                                viewAllSearchParam = searchParams,
                                isError = false,
                                isLoadingTopResult = false,
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

    private fun searchAllTracks(searchParams: String) {
        viewModelScope.launch {
            searchRepository.searchAllTracks(searchParams).collectLatest { response ->
                val resource = when (response) {
                    is Resource.Loading -> Resource.Loading()
                    is Resource.Error -> Resource.Error(message = response.message!!)
                    is Resource.Success -> Resource.Success(data = response.data!!.result)
                }

                updateSearchState {
                    copy(viewAllTracks = resource)
                }
            }
        }
    }

    private fun searchAllAlbums(searchParams: String) {
        viewModelScope.launch {
            searchRepository.searchAllAlbums(searchParams).collectLatest { response ->
                val resource = when (response) {
                    is Resource.Loading -> Resource.Loading()
                    is Resource.Error -> Resource.Error(message = response.message!!)
                    is Resource.Success -> Resource.Success(data = response.data!!.result)
                }

                updateSearchState {
                    copy(viewAllAlbums = resource)
                }
            }
        }
    }

    private fun searchAllArtists(searchParams: String) {
        viewModelScope.launch {
            searchRepository.searchAllArtists(searchParams).collectLatest { response ->
                val resource = when (response) {
                    is Resource.Loading -> Resource.Loading()
                    is Resource.Error -> Resource.Error(message = response.message!!)
                    is Resource.Success -> Resource.Success(data = response.data!!.result)
                }

                updateSearchState {
                    copy(viewAllArtists = resource)
                }
            }
        }
    }

    private fun clearSearchAllResources() {
        updateSearchState {
            copy(
                viewAllTracks = null,
                viewAllAlbums = null,
                viewAllArtists = null
            )
        }
    }

    private fun toggleSearchTrackFavorite(trackHash: String, isFavorite: Boolean) {
        viewModelScope.launch {
            // Optimistically update the UI
            _searchState.update { currentState ->
                val updatedTracks = currentState.viewAllTracks?.data.orEmpty().map { track ->
                    if (track.trackHash == trackHash) {
                        track.copy(isFavorite = !isFavorite)
                    } else {
                        track
                    }
                }
                currentState.copy(
                    viewAllTracks = Resource.Success(updatedTracks)
                )
            }

            val request = if (isFavorite) {
                pLayerRepository.removeTrackFromFavorite(trackHash)
            } else {
                pLayerRepository.addTrackToFavorite(trackHash)
            }

            request.collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        // No-op
                    }

                    is Resource.Success -> {
                        _searchState.update { currentState ->
                            val updatedTracks =
                                currentState.viewAllTracks?.data.orEmpty().map { track ->
                                    if (track.trackHash == trackHash) {
                                        track.copy(isFavorite = result.data ?: false)
                                    } else {
                                        track
                                    }
                                }
                            currentState.copy(
                                viewAllTracks = Resource.Success(updatedTracks)
                            )
                        }
                    }

                    is Resource.Error -> {
                        _searchState.update { currentState ->
                            val revertedTracks =
                                currentState.viewAllTracks?.data.orEmpty().map { track ->
                                    if (track.trackHash == trackHash) {
                                        track.copy(isFavorite = isFavorite) // revert
                                    } else {
                                        track
                                    }
                                }
                            currentState.copy(
                                viewAllTracks = Resource.Success(revertedTracks)
                            )
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

            is SearchUiEvent.OnRetrySearchTopResults -> {
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

            is SearchUiEvent.OnSearchAllTacks -> {
                searchAllTracks(event.searchParams)
            }

            is SearchUiEvent.OnSearchAllAlbums -> {
                searchAllAlbums(event.searchParams)
            }

            is SearchUiEvent.OnSearchAllArtists -> {
                searchAllArtists(event.searchParams)
            }

            is SearchUiEvent.OnClearSearchAllResources -> {
                clearSearchAllResources()
            }

            is SearchUiEvent.OnToggleTrackFavorite -> {
                toggleSearchTrackFavorite(
                    trackHash = event.trackHash,
                    isFavorite = event.isFavorite
                )
            }
        }
    }
}
