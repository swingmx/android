package com.android.swingmusic.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.library.presentation.event.PlaylistsUiEffect
import com.android.swingmusic.library.presentation.event.PlaylistsUiEvent
import com.android.swingmusic.library.presentation.state.PlaylistsUiState
import com.android.swingmusic.playlist.domain.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<PlaylistsUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var hasLoaded = false

    init {
        viewModelScope.launch { val url = playlistRepository.getBaseUrl()
            updateUiState { copy(baseUrl = url) } }
        getPlaylists()
    }

    fun onEvent(event: PlaylistsUiEvent) {
        when (event) {
            // A playlist may have been renamed, pinned or deleted on its page
            PlaylistsUiEvent.OnScreenResumed -> if (hasLoaded) getPlaylists(quiet = true)
            PlaylistsUiEvent.OnBackClicked -> _uiEffect.trySend(PlaylistsUiEffect.NavigateBack)
            PlaylistsUiEvent.OnRetry -> getPlaylists()
            is PlaylistsUiEvent.OnPlaylistClicked ->
                _uiEffect.trySend(PlaylistsUiEffect.NavigateToPlaylist(event.playlistId))

            PlaylistsUiEvent.OnNewPlaylistClicked -> updateUiState {
                copy(showNewPlaylistDialog = true, newPlaylistName = "", newPlaylistError = null)
            }

            is PlaylistsUiEvent.OnNewPlaylistNameChanged -> updateUiState {
                copy(newPlaylistName = event.name, newPlaylistError = null)
            }

            PlaylistsUiEvent.OnCreatePlaylist -> createPlaylist()
            PlaylistsUiEvent.OnNewPlaylistDismissed -> updateUiState { copy(showNewPlaylistDialog = false) }
        }
    }

    private fun getPlaylists(quiet: Boolean = false) {
        if (!quiet) updateUiState { copy(isLoadingPlaylists = true, errorLoadingPlaylists = null) }
        viewModelScope.launch {
            when (val result = playlistRepository.getAllPlaylists()) {
                is Resource.Success -> {
                    hasLoaded = true
                    val (pinned, others) = result.data.orEmpty().partition { it.pinned }
                    updateUiState {
                        copy(
                            isLoadingPlaylists = false,
                            errorLoadingPlaylists = null,
                            pinnedPlaylists = pinned,
                            otherPlaylists = others
                        )
                    }
                }

                else -> if (!quiet) updateUiState {
                    copy(isLoadingPlaylists = false, errorLoadingPlaylists = result.message)
                }
            }
        }
    }

    private fun createPlaylist() {
        val name = _uiState.value.newPlaylistName.trim()
        if (name.isEmpty() || _uiState.value.isCreatingPlaylist) return
        updateUiState { copy(isCreatingPlaylist = true) }
        viewModelScope.launch {
            val result = playlistRepository.createPlaylist(name)
            if (result is Resource.Success) {
                updateUiState { copy(isCreatingPlaylist = false, showNewPlaylistDialog = false) }
                _uiEffect.trySend(PlaylistsUiEffect.ShowSnackbar("Created $name"))
                getPlaylists(quiet = true)
            } else {
                updateUiState { copy(isCreatingPlaylist = false, newPlaylistError = result.message) }
            }
        }
    }

    private fun updateUiState(block: PlaylistsUiState.() -> PlaylistsUiState) {
        _uiState.update(block)
    }
}
