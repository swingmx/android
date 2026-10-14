package com.android.swingmusic.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.avatar.AvatarStore
import com.android.swingmusic.library.domain.LibraryRepository
import com.android.swingmusic.library.presentation.event.LibraryUiEffect
import com.android.swingmusic.library.presentation.event.LibraryUiEvent
import com.android.swingmusic.library.presentation.state.LibraryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val avatarStore: AvatarStore,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<LibraryUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            avatarStore.avatar.collect { file -> updateUiState { copy(avatarPath = file?.absolutePath) } }
        }
        viewModelScope.launch {
            val url = libraryRepository.getBaseUrl()
            updateUiState { copy(baseUrl = url) }
        }
    }

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            // Counts change as playlists are made elsewhere, so they refresh whenever Library shows
            LibraryUiEvent.OnScreenResumed -> loadCounts()
            LibraryUiEvent.OnProfileClicked -> _uiEffect.trySend(LibraryUiEffect.NavigateToProfile)
            LibraryUiEvent.OnPlaylistsClicked -> _uiEffect.trySend(LibraryUiEffect.NavigateToPlaylists)
            LibraryUiEvent.OnAlbumsClicked -> _uiEffect.trySend(LibraryUiEffect.NavigateToAlbums)
            LibraryUiEvent.OnArtistsClicked -> _uiEffect.trySend(LibraryUiEffect.NavigateToArtists)
            LibraryUiEvent.OnFoldersClicked -> _uiEffect.trySend(LibraryUiEffect.NavigateToFolders)
        }
    }

    private fun loadCounts() {
        viewModelScope.launch {
            val counts = libraryRepository.getCounts()
            updateUiState { copy(isLoadingCounts = false, counts = counts) }
        }
    }


    private fun updateUiState(block: LibraryUiState.() -> LibraryUiState) {
        _uiState.update(block)
    }
}
