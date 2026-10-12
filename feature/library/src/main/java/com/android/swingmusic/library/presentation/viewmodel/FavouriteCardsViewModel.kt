package com.android.swingmusic.library.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.library.domain.FavouritesRepository
import com.android.swingmusic.library.domain.LibraryRepository
import com.android.swingmusic.library.presentation.event.FavouriteCardsUiEffect
import com.android.swingmusic.library.presentation.event.FavouriteCardsUiEvent
import com.android.swingmusic.library.presentation.state.FavouriteCardsKind
import com.android.swingmusic.library.presentation.state.FavouriteCardsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FavouriteCardsViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val favouritesRepository: FavouritesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val kind = savedStateHandle.get<String>("kind")
        ?.let { name -> FavouriteCardsKind.entries.firstOrNull { it.name == name } }
        ?: FavouriteCardsKind.ALBUMS

    private val _uiState = MutableStateFlow(FavouriteCardsUiState(kind = kind))
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<FavouriteCardsUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var hasLoaded = false

    init {
        viewModelScope.launch {
            val url = libraryRepository.getBaseUrl()
            updateUiState { copy(baseUrl = url) }
        }
        getItems()
    }

    fun onEvent(event: FavouriteCardsUiEvent) {
        when (event) {
            // An album or artist may have been unfavourited on its page
            FavouriteCardsUiEvent.OnScreenResumed -> if (hasLoaded) getItems(quiet = true)
            FavouriteCardsUiEvent.OnBackClicked -> _uiEffect.trySend(FavouriteCardsUiEffect.NavigateBack)
            FavouriteCardsUiEvent.OnRetry -> getItems()
            FavouriteCardsUiEvent.OnLoadMoreItems -> loadMore()
            is FavouriteCardsUiEvent.OnItemClicked -> _uiEffect.trySend(
                if (kind == FavouriteCardsKind.ALBUMS) FavouriteCardsUiEffect.NavigateToAlbum(event.hash)
                else FavouriteCardsUiEffect.NavigateToArtist(event.hash)
            )
        }
    }

    private fun getItems(quiet: Boolean = false) {
        if (!quiet) updateUiState { copy(isLoadingItems = true, errorLoadingItems = null) }
        viewModelScope.launch {
            // A quiet refresh reloads as many as are showing, so the grid keeps its place
            val limit = maxOf(PAGE_SIZE, _uiState.value.itemCount)
            if (kind == FavouriteCardsKind.ALBUMS) {
                when (val result = favouritesRepository.getAlbums(start = 0, limit = limit)) {
                    is Resource.Success -> {
                        hasLoaded = true
                        val items = result.data?.items.orEmpty()
                        updateUiState {
                            copy(isLoadingItems = false, albums = items, total = result.data?.total, endReached = items.size < limit)
                        }
                    }

                    else -> if (!quiet) updateUiState { copy(isLoadingItems = false, errorLoadingItems = result.message) }
                }
            } else {
                when (val result = favouritesRepository.getArtists(start = 0, limit = limit)) {
                    is Resource.Success -> {
                        hasLoaded = true
                        val items = result.data?.items.orEmpty()
                        updateUiState {
                            copy(isLoadingItems = false, artists = items, total = result.data?.total, endReached = items.size < limit)
                        }
                    }

                    else -> if (!quiet) updateUiState { copy(isLoadingItems = false, errorLoadingItems = result.message) }
                }
            }
        }
    }

    private fun loadMore() {
        val state = _uiState.value
        if (state.endReached || state.isLoadingMoreItems || state.isLoadingItems) return
        updateUiState { copy(isLoadingMoreItems = true) }
        viewModelScope.launch {
            if (kind == FavouriteCardsKind.ALBUMS) {
                val items = (favouritesRepository.getAlbums(state.itemCount, PAGE_SIZE) as? Resource.Success)?.data?.items
                updateUiState {
                    copy(isLoadingMoreItems = false, albums = albums + items.orEmpty(), endReached = items == null || items.size < PAGE_SIZE)
                }
            } else {
                val items = (favouritesRepository.getArtists(state.itemCount, PAGE_SIZE) as? Resource.Success)?.data?.items
                updateUiState {
                    copy(isLoadingMoreItems = false, artists = artists + items.orEmpty(), endReached = items == null || items.size < PAGE_SIZE)
                }
            }
        }
    }

    private fun updateUiState(block: FavouriteCardsUiState.() -> FavouriteCardsUiState) {
        _uiState.update(block)
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}
