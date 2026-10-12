package com.android.swingmusic.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.library.domain.FavouriteType
import com.android.swingmusic.library.domain.FavouritesRepository
import com.android.swingmusic.library.domain.LibraryRepository
import com.android.swingmusic.library.presentation.event.FavouriteTracksUiEffect
import com.android.swingmusic.library.presentation.event.FavouriteTracksUiEvent
import com.android.swingmusic.library.presentation.state.FavouriteTracksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FavouriteTracksViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val favouritesRepository: FavouritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavouriteTracksUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<FavouriteTracksUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var lastRemoved: Pair<Track, Int>? = null

    init {
        viewModelScope.launch {
            val url = libraryRepository.getBaseUrl()
            updateUiState { copy(baseUrl = url) }
        }
        getTracks()
    }

    fun onEvent(event: FavouriteTracksUiEvent) {
        when (event) {
            FavouriteTracksUiEvent.OnBackClicked -> _uiEffect.trySend(FavouriteTracksUiEffect.NavigateBack)
            FavouriteTracksUiEvent.OnRetry -> getTracks()
            FavouriteTracksUiEvent.OnLoadMoreTracks -> loadMore()
            FavouriteTracksUiEvent.OnPlayClicked -> playAll(0, shuffle = false)
            FavouriteTracksUiEvent.OnShuffleClicked -> playAll(0, shuffle = true)
            is FavouriteTracksUiEvent.OnTrackClicked -> playAll(event.index, shuffle = false)
            is FavouriteTracksUiEvent.OnUnfavouriteTrack -> unfavourite(event.track)
            FavouriteTracksUiEvent.OnUndoUnfavourite -> undo()
        }
    }

    private fun getTracks() {
        updateUiState { copy(isLoadingTracks = true, errorLoadingTracks = null) }
        viewModelScope.launch {
            when (val result = favouritesRepository.getTracks(start = 0, limit = PAGE_SIZE)) {
                is Resource.Success -> {
                    val page = result.data
                    updateUiState {
                        copy(
                            isLoadingTracks = false,
                            tracks = page?.items.orEmpty(),
                            total = page?.total,
                            endReached = page?.items.orEmpty().size < PAGE_SIZE
                        )
                    }
                }

                else -> updateUiState { copy(isLoadingTracks = false, errorLoadingTracks = result.message) }
            }
        }
    }

    private fun loadMore() {
        val state = _uiState.value
        if (state.endReached || state.isLoadingMoreTracks || state.isLoadingTracks) return
        updateUiState { copy(isLoadingMoreTracks = true) }
        viewModelScope.launch {
            val result = favouritesRepository.getTracks(start = state.tracks.size, limit = PAGE_SIZE)
            val items = (result as? Resource.Success)?.data?.items
            updateUiState {
                copy(
                    isLoadingMoreTracks = false,
                    tracks = tracks + items.orEmpty(),
                    endReached = items == null || items.size < PAGE_SIZE
                )
            }
        }
    }

    private fun playAll(startIndex: Int, shuffle: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            val tracks = if (state.endReached) {
                state.tracks
            } else {
                (favouritesRepository.getTracks(start = 0, limit = ALL) as? Resource.Success)?.data?.items
                    ?.also { all -> updateUiState { copy(tracks = all, endReached = true) } }
                    ?: state.tracks
            }
            if (tracks.isNotEmpty()) {
                _uiEffect.trySend(
                    FavouriteTracksUiEffect.PlayTracks(tracks, startIndex.coerceIn(0, tracks.lastIndex), shuffle)
                )
            }
        }
    }

    private fun unfavourite(track: Track) {
        val index = _uiState.value.tracks.indexOfFirst { it.trackHash == track.trackHash }
        if (index < 0) return
        lastRemoved = track to index
        updateUiState {
            copy(tracks = tracks.filterIndexed { i, _ -> i != index })
        }
        viewModelScope.launch {
            val result = favouritesRepository.setFavourite(track.trackHash, FavouriteType.TRACK, isFavourite = false)
            if (result is Resource.Success) {
                refreshTotal()
                _uiEffect.trySend(FavouriteTracksUiEffect.ShowSnackbar("Removed from favourites", undoable = true))
            } else {
                restore(track, index)
                _uiEffect.trySend(FavouriteTracksUiEffect.ShowSnackbar(result.message ?: "Couldn't update favourites"))
            }
        }
    }

    private fun undo() {
        val (track, index) = lastRemoved ?: return
        lastRemoved = null
        restore(track, index)
        viewModelScope.launch {
            favouritesRepository.setFavourite(track.trackHash, FavouriteType.TRACK, isFavourite = true)
            refreshTotal()
        }
    }

    // The server only gives the total with the first page, so it's fetched rather than counted locally
    private suspend fun refreshTotal() {
        val total = (favouritesRepository.getTracks(start = 0, limit = 1) as? Resource.Success)?.data?.total
        if (total != null) updateUiState { copy(total = total) }
    }

    private fun restore(track: Track, index: Int) {
        updateUiState {
            copy(
                tracks = tracks.toMutableList().apply { add(index.coerceAtMost(size), track.copy(isFavorite = true)) }
            )
        }
    }

    private fun updateUiState(block: FavouriteTracksUiState.() -> FavouriteTracksUiState) {
        _uiState.update(block)
    }

    private companion object {
        const val PAGE_SIZE = 50
        const val ALL = -1
    }
}
