package com.android.swingmusic.album.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.android.swingmusic.album.domain.AlbumRepository
import com.android.swingmusic.album.presentation.event.AlbumsUiEvent
import com.android.swingmusic.album.presentation.state.AllAlbumsUiState
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.util.SortBy
import com.android.swingmusic.core.domain.util.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllAlbumsViewModel @Inject constructor(
    private val artistRepository: AlbumRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _baseUrl: MutableStateFlow<String?> = MutableStateFlow(null)
    val baseUrl: StateFlow<String?> get() = _baseUrl

    private val _allAlbumsUiState = mutableStateOf(AllAlbumsUiState())
    val allAlbumsUiState: State<AllAlbumsUiState> = _allAlbumsUiState

    val sortAlbumsByEntries: List<Pair<SortBy, String>> = listOf(
        Pair(SortBy.DATE, "date"),
        Pair(SortBy.NO_OF_TRACKS, "trackcount"),
        Pair(SortBy.DURATION, "duration"),
        Pair(SortBy.TITLE, "title"),
        Pair(SortBy.CREATED_DATE, "created_date"),
        Pair(SortBy.PLAY_DURATION, "playduration"),
        Pair(SortBy.PLAY_COUNT, "playcount"),
        Pair(SortBy.LAST_PLAYED, "lastplayed"),
        Pair(SortBy.ALBUM_ARTISTS, "albumartists"),
    )

    private fun getAlbumCount() {
        viewModelScope.launch {
            artistRepository.getAlbumCount().collectLatest {
                _allAlbumsUiState.value = _allAlbumsUiState.value.copy(totalAlbums = it)
            }
        }
    }

    private fun getPagingAlbums(sortBy: String, sortOrder: SortOrder) {
        val order = when (sortOrder) {
            SortOrder.DESCENDING -> 1
            SortOrder.ASCENDING -> 0
        }
        viewModelScope.launch {
            _allAlbumsUiState.value = _allAlbumsUiState.value.copy(
                pagingAlbums = artistRepository.getPagingAlbums(
                    sortBy = sortBy,
                    sortOrder = order
                ).cachedIn(viewModelScope)
            )
        }
    }

    init {
        getBaseUrl()
    }


    private fun getBaseUrl() {
        _baseUrl.value = authRepository.getBaseUrl()
    }

    init {
        getPagingAlbums(
            sortBy = _allAlbumsUiState.value.sortBy.second,
            sortOrder = _allAlbumsUiState.value.sortOrder
        )
        getAlbumCount()
    }


    fun onAlbumsUiEvent(event: AlbumsUiEvent) {
        when (event) {
            is AlbumsUiEvent.OnSortBy -> {
                // Retry fetching artist count if the previous sorting resulted to Error
                if (_allAlbumsUiState.value.totalAlbums is Resource.Error) {
                    getAlbumCount()
                }

                if (event.sortByPair == _allAlbumsUiState.value.sortBy) {
                    val newOrder = if (_allAlbumsUiState.value.sortOrder == SortOrder.ASCENDING)
                        SortOrder.DESCENDING else SortOrder.ASCENDING

                    _allAlbumsUiState.value = _allAlbumsUiState.value.copy(sortOrder = newOrder)
                    getPagingAlbums(
                        sortBy = event.sortByPair.second,
                        sortOrder = newOrder
                    )
                } else {
                    _allAlbumsUiState.value = _allAlbumsUiState.value.copy(
                        sortBy = event.sortByPair,
                        sortOrder = SortOrder.DESCENDING
                    )
                    getPagingAlbums(
                        sortBy = event.sortByPair.second,
                        sortOrder = SortOrder.DESCENDING
                    )
                }
            }

            is AlbumsUiEvent.OnClickAlbum -> {
                // TODO: Navigate from the UI (apparently not in the VM)
            }

            is AlbumsUiEvent.OnUpdateGridCount -> {
                _allAlbumsUiState.value = _allAlbumsUiState.value.copy(
                    gridCount = event.newCount
                )
            }

            is AlbumsUiEvent.OnRetry -> {
                if (_allAlbumsUiState.value.totalAlbums is Resource.Error) {
                    getAlbumCount()
                }
            }
        }
    }
}
