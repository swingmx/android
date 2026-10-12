package com.android.swingmusic.playlist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.playlist.domain.PlaylistRepository
import com.android.swingmusic.playlist.domain.PlaylistTarget
import com.android.swingmusic.playlist.presentation.event.AddToPlaylistUiEffect
import com.android.swingmusic.playlist.presentation.event.AddToPlaylistUiEvent
import com.android.swingmusic.playlist.presentation.state.AddToPlaylistUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AddToPlaylistViewModel.Factory::class)
internal class AddToPlaylistViewModel @AssistedInject constructor(
    @Assisted private val target: PlaylistTarget,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(target: PlaylistTarget): AddToPlaylistViewModel
    }

    private val _uiState = MutableStateFlow(AddToPlaylistUiState(newPlaylistName = target.suggestedName))
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<AddToPlaylistUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        getPlaylists()
    }

    fun onEvent(event: AddToPlaylistUiEvent) {
        when (event) {
            AddToPlaylistUiEvent.OnRetry -> getPlaylists()
            is AddToPlaylistUiEvent.OnPlaylistClicked -> addToPlaylist(event.playlist)
            AddToPlaylistUiEvent.OnNewPlaylistClicked -> updateUiState { copy(showNewPlaylistField = true) }
            AddToPlaylistUiEvent.OnCloseNewPlaylist -> updateUiState {
                copy(
                    showNewPlaylistField = false,
                    newPlaylistName = target.suggestedName,
                    newPlaylistError = null
                )
            }
            is AddToPlaylistUiEvent.OnNewPlaylistNameChanged -> updateUiState {
                copy(newPlaylistName = event.name, newPlaylistError = null)
            }
            AddToPlaylistUiEvent.OnCreatePlaylist -> createPlaylist()
        }
    }

    private fun getPlaylists() {
        updateUiState { copy(isLoadingPlaylists = true, errorLoadingPlaylists = null) }
        viewModelScope.launch {
            when (val result = playlistRepository.getAllPlaylists()) {
                is Resource.Success -> updateUiState {
                    copy(isLoadingPlaylists = false, playlists = result.data.orEmpty())
                }
                else -> updateUiState {
                    copy(isLoadingPlaylists = false, errorLoadingPlaylists = result.message)
                }
            }
        }
    }

    private fun addToPlaylist(playlist: Playlist) {
        if (_uiState.value.isSaving) return
        updateUiState { copy(isSaving = true, errorAddingToPlaylist = null) }
        viewModelScope.launch {
            val result = playlistRepository.addToPlaylist(playlist.id, target)
            updateUiState { copy(isSaving = false, errorAddingToPlaylist = result.message) }
            if (result is Resource.Success) {
                _uiEffect.trySend(AddToPlaylistUiEffect.Done("Added to ${playlist.name}"))
            }
        }
    }

    private fun createPlaylist() {
        val name = _uiState.value.newPlaylistName.trim()
        if (_uiState.value.isSaving) return
        if (name.isEmpty()) {
            updateUiState { copy(newPlaylistError = "Enter a name") }
            return
        }
        updateUiState { copy(isSaving = true) }
        viewModelScope.launch {
            val result = playlistRepository.saveAsPlaylist(name, target)
            if (result is Resource.Success) {
                updateUiState { copy(isSaving = false) }
                _uiEffect.trySend(AddToPlaylistUiEffect.Done("Created $name"))
            } else {
                updateUiState { copy(isSaving = false, newPlaylistError = result.message) }
            }
        }
    }

    private fun updateUiState(block: AddToPlaylistUiState.() -> AddToPlaylistUiState) {
        _uiState.update(block)
    }
}
