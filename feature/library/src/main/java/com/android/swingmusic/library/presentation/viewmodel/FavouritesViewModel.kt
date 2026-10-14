package com.android.swingmusic.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.avatar.AvatarStore
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.library.domain.FavouriteType
import com.android.swingmusic.library.domain.FavouritesRepository
import com.android.swingmusic.library.domain.LibraryRepository
import com.android.swingmusic.library.presentation.event.FavouritesUiEffect
import com.android.swingmusic.library.presentation.event.FavouritesUiEvent
import com.android.swingmusic.library.presentation.state.FavouritesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FavouritesViewModel @Inject constructor(
    private val avatarStore: AvatarStore,
    private val libraryRepository: LibraryRepository,
    private val favouritesRepository: FavouritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavouritesUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<FavouritesUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var hasLoaded = false
    private var lastRemoved: Pair<Track, Int>? = null

    init {
        viewModelScope.launch {
            avatarStore.avatar.collect { file -> updateUiState { copy(avatarPath = file?.absolutePath) } }
        }
        viewModelScope.launch {
            val url = libraryRepository.getBaseUrl()
            updateUiState { copy(baseUrl = url) }
        }
        getFavourites()
    }

    fun onEvent(event: FavouritesUiEvent) {
        when (event) {
            // Favourites change from album, artist and track pages, so they refresh when the tab shows
            FavouritesUiEvent.OnScreenResumed -> if (hasLoaded) getFavourites(quiet = true)
            FavouritesUiEvent.OnProfileClicked -> _uiEffect.trySend(FavouritesUiEffect.NavigateToProfile)
            FavouritesUiEvent.OnRetry -> getFavourites()
            FavouritesUiEvent.OnPlayClicked -> playAll(startIndex = 0, shuffle = false)
            FavouritesUiEvent.OnShuffleClicked -> playAll(startIndex = 0, shuffle = true)
            is FavouritesUiEvent.OnTrackClicked -> playAll(startIndex = event.index, shuffle = false)
            is FavouritesUiEvent.OnUnfavouriteTrack -> unfavourite(event.track)
            FavouritesUiEvent.OnUndoUnfavourite -> undoUnfavourite()
            FavouritesUiEvent.OnSeeAllTracksClicked -> _uiEffect.trySend(FavouritesUiEffect.NavigateToTracks)
            FavouritesUiEvent.OnSeeAllAlbumsClicked -> _uiEffect.trySend(FavouritesUiEffect.NavigateToAlbums)
            FavouritesUiEvent.OnSeeAllArtistsClicked -> _uiEffect.trySend(FavouritesUiEffect.NavigateToArtists)
            is FavouritesUiEvent.OnAlbumClicked -> _uiEffect.trySend(FavouritesUiEffect.NavigateToAlbum(event.albumHash))
            is FavouritesUiEvent.OnArtistClicked -> _uiEffect.trySend(FavouritesUiEffect.NavigateToArtist(event.artistHash))
        }
    }

    private fun getFavourites(quiet: Boolean = false) {
        if (!quiet) updateUiState { copy(isLoadingFavourites = true, errorLoadingFavourites = null) }
        viewModelScope.launch {
            val result = favouritesRepository.getSummary(
                trackLimit = SUMMARY_TRACKS,
                albumLimit = SUMMARY_CARDS,
                artistLimit = SUMMARY_CARDS
            )
            when (result) {
                is Resource.Success -> {
                    hasLoaded = true
                    updateUiState {
                        copy(isLoadingFavourites = false, errorLoadingFavourites = null, favourites = result.data)
                    }
                }

                else -> if (!quiet) updateUiState {
                    copy(isLoadingFavourites = false, errorLoadingFavourites = result.message)
                }
            }
        }
    }

    /** All favourite tracks are queued, so the full list is fetched first. */
    private fun playAll(startIndex: Int, shuffle: Boolean) {
        viewModelScope.launch {
            val result = favouritesRepository.getTracks(start = 0, limit = ALL)
            val tracks = (result as? Resource.Success)?.data?.items.orEmpty()
            if (tracks.isEmpty()) {
                _uiEffect.trySend(FavouritesUiEffect.ShowSnackbar(result.message ?: "No favourite tracks yet"))
                return@launch
            }
            _uiEffect.trySend(
                FavouritesUiEffect.PlayTracks(tracks, startIndex.coerceIn(0, tracks.lastIndex), shuffle)
            )
        }
    }

    private fun unfavourite(track: Track) {
        val summary = _uiState.value.favourites ?: return
        val index = summary.tracks.indexOfFirst { it.trackHash == track.trackHash }
        if (index < 0) return
        lastRemoved = track to index
        updateUiState {
            copy(
                favourites = summary.copy(
                    tracks = summary.tracks.filterIndexed { i, _ -> i != index },
                    trackCount = (summary.trackCount - 1).coerceAtLeast(0)
                )
            )
        }
        viewModelScope.launch {
            val result = favouritesRepository.setFavourite(track.trackHash, FavouriteType.TRACK, isFavourite = false)
            if (result is Resource.Success) {
                _uiEffect.trySend(FavouritesUiEffect.ShowSnackbar("Removed from favourites", undoable = true))
            } else {
                restore(track, index)
                _uiEffect.trySend(FavouritesUiEffect.ShowSnackbar(result.message ?: "Couldn't update favourites"))
            }
        }
    }

    private fun undoUnfavourite() {
        val (track, index) = lastRemoved ?: return
        lastRemoved = null
        restore(track, index)
        viewModelScope.launch {
            val result = favouritesRepository.setFavourite(track.trackHash, FavouriteType.TRACK, isFavourite = true)
            if (result !is Resource.Success) getFavourites(quiet = true)
        }
    }

    private fun restore(track: Track, index: Int) {
        updateUiState {
            val summary = favourites ?: return@updateUiState this
            val tracks = summary.tracks.toMutableList().apply { add(index.coerceAtMost(size), track.copy(isFavorite = true)) }
            copy(favourites = summary.copy(tracks = tracks, trackCount = summary.trackCount + 1))
        }
    }

    private fun updateUiState(block: FavouritesUiState.() -> FavouritesUiState) {
        _uiState.update(block)
    }

    private companion object {
        const val SUMMARY_TRACKS = 4
        const val SUMMARY_CARDS = 12
        const val ALL = -1
    }
}
