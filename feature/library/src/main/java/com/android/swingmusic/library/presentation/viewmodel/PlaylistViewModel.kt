package com.android.swingmusic.library.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.library.domain.FavouriteType
import com.android.swingmusic.library.domain.FavouritesRepository
import com.android.swingmusic.library.presentation.event.PlaylistUiEffect
import com.android.swingmusic.library.presentation.event.PlaylistUiEvent
import com.android.swingmusic.library.presentation.state.PlaylistUiState
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
internal class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val favouritesRepository: FavouritesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val playlistId: String = savedStateHandle.get<String>("playlistId") ?: ""

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<PlaylistUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch { val url = playlistRepository.getBaseUrl()
            updateUiState { copy(baseUrl = url) } }
        getPlaylist()
    }

    fun onEvent(event: PlaylistUiEvent) {
        when (event) {
            PlaylistUiEvent.OnBackClicked -> _uiEffect.trySend(PlaylistUiEffect.NavigateBack)
            PlaylistUiEvent.OnRetry -> getPlaylist()
            PlaylistUiEvent.OnLoadMoreTracks -> loadMoreTracks()
            PlaylistUiEvent.OnPlayClicked -> play(startIndex = 0, shuffle = false)
            PlaylistUiEvent.OnShuffleClicked -> play(startIndex = 0, shuffle = true)
            is PlaylistUiEvent.OnTrackClicked -> play(startIndex = event.index, shuffle = false)
            is PlaylistUiEvent.OnToggleTrackFavourite -> toggleTrackFavourite(event.trackHash, event.isFavourite)
            is PlaylistUiEvent.OnRemoveTrack -> removeTrack(event.track, event.index)

            PlaylistUiEvent.OnMoreClicked -> updateUiState { copy(showOptionsSheet = true) }
            PlaylistUiEvent.OnOptionsDismissed -> updateUiState { copy(showOptionsSheet = false) }
            PlaylistUiEvent.OnRenameClicked -> updateUiState {
                copy(
                    showOptionsSheet = false,
                    showRenameDialog = true,
                    renameText = playlist?.name.orEmpty(),
                    renameError = null
                )
            }

            is PlaylistUiEvent.OnRenameTextChanged -> updateUiState { copy(renameText = event.text, renameError = null) }
            PlaylistUiEvent.OnRenameConfirmed -> rename()
            PlaylistUiEvent.OnRenameDismissed -> updateUiState { copy(showRenameDialog = false) }
            PlaylistUiEvent.OnChangeImageClicked -> {
                updateUiState { copy(showOptionsSheet = false) }
                _uiEffect.trySend(PlaylistUiEffect.LaunchImagePicker)
            }

            is PlaylistUiEvent.OnImagePicked -> setImage(event.uri)
            PlaylistUiEvent.OnRemoveImageClicked -> edit("Image removed") {
                playlistRepository.removePlaylistImage(playlistId)
            }

            PlaylistUiEvent.OnTogglePinClicked -> {
                val pinned = _uiState.value.playlist?.pinned == true
                edit(if (pinned) "Unpinned" else "Pinned") { playlistRepository.togglePin(playlistId) }
            }

            PlaylistUiEvent.OnDeleteClicked -> updateUiState { copy(showOptionsSheet = false, showDeleteDialog = true) }
            PlaylistUiEvent.OnDeleteConfirmed -> delete()
            PlaylistUiEvent.OnDeleteDismissed -> updateUiState { copy(showDeleteDialog = false) }
        }
    }

    private fun getPlaylist(quiet: Boolean = false) {
        if (!quiet) updateUiState { copy(isLoadingPlaylist = true, errorLoadingPlaylist = null) }
        viewModelScope.launch {
            when (val result = playlistRepository.getPlaylist(playlistId, start = 0, limit = PAGE_SIZE)) {
                is Resource.Success -> updateUiState {
                    copy(
                        isLoadingPlaylist = false,
                        errorLoadingPlaylist = null,
                        playlist = result.data?.playlist,
                        tracks = result.data?.tracks.orEmpty()
                    )
                }

                else -> if (!quiet) updateUiState {
                    copy(isLoadingPlaylist = false, errorLoadingPlaylist = result.message)
                }
            }
        }
    }

    private fun loadMoreTracks() {
        val state = _uiState.value
        if (!state.hasMoreTracks || state.isLoadingMoreTracks) return
        updateUiState { copy(isLoadingMoreTracks = true) }
        viewModelScope.launch {
            val result = playlistRepository.getPlaylist(playlistId, start = state.tracks.size, limit = PAGE_SIZE)
            updateUiState {
                copy(
                    isLoadingMoreTracks = false,
                    tracks = if (result is Resource.Success) tracks + result.data?.tracks.orEmpty() else tracks
                )
            }
        }
    }

    /** The whole playlist is queued, so any tracks not loaded yet are fetched first. */
    private fun play(startIndex: Int, shuffle: Boolean) {
        val state = _uiState.value
        val playlist = state.playlist ?: return
        viewModelScope.launch {
            val tracks = if (state.hasMoreTracks) {
                val result = playlistRepository.getPlaylist(playlistId, start = 0, limit = playlist.trackCount)
                (result as? Resource.Success)?.data?.tracks?.also { all ->
                    updateUiState { copy(tracks = all) }
                } ?: state.tracks
            } else {
                state.tracks
            }
            if (tracks.isNotEmpty()) {
                _uiEffect.trySend(
                    PlaylistUiEffect.PlayTracks(tracks, startIndex.coerceIn(0, tracks.lastIndex), shuffle)
                )
            }
        }
    }

    private fun toggleTrackFavourite(trackHash: String, isFavourite: Boolean) {
        setTrackFavourite(trackHash, !isFavourite)
        viewModelScope.launch {
            val result = favouritesRepository.setFavourite(trackHash, FavouriteType.TRACK, !isFavourite)
            if (result !is Resource.Success) {
                setTrackFavourite(trackHash, isFavourite)
                _uiEffect.trySend(PlaylistUiEffect.ShowSnackbar(result.message ?: "Couldn't update favourites"))
            }
        }
    }

    private fun setTrackFavourite(trackHash: String, isFavourite: Boolean) {
        updateUiState {
            copy(tracks = tracks.map { if (it.trackHash == trackHash) it.copy(isFavorite = isFavourite) else it })
        }
    }

    private fun removeTrack(track: Track, index: Int) {
        val before = _uiState.value
        updateUiState {
            copy(
                tracks = tracks.filterIndexed { i, _ -> i != index },
                playlist = playlist?.copy(trackCount = (playlist.trackCount - 1).coerceAtLeast(0))
            )
        }
        viewModelScope.launch {
            val result = playlistRepository.removeTrack(playlistId, track.trackHash, index)
            if (result is Resource.Success) {
                _uiEffect.trySend(PlaylistUiEffect.ShowSnackbar("Removed ${track.title}"))
            } else {
                updateUiState { copy(tracks = before.tracks, playlist = before.playlist) }
                _uiEffect.trySend(PlaylistUiEffect.ShowSnackbar(result.message ?: "Couldn't remove the track"))
            }
        }
    }

    private fun rename() {
        val playlist = _uiState.value.playlist ?: return
        val name = _uiState.value.renameText.trim()
        if (name.isEmpty() || _uiState.value.isSaving) return
        if (name == playlist.name) {
            updateUiState { copy(showRenameDialog = false) }
            return
        }
        updateUiState { copy(isSaving = true) }
        viewModelScope.launch {
            val result = playlistRepository.renamePlaylist(playlist, name)
            if (result is Resource.Success) {
                updateUiState { copy(isSaving = false, showRenameDialog = false, playlist = playlist.copy(name = name)) }
                _uiEffect.trySend(PlaylistUiEffect.ShowSnackbar("Renamed to $name"))
            } else {
                updateUiState { copy(isSaving = false, renameError = result.message) }
            }
        }
    }

    private fun setImage(uri: String) {
        val playlist = _uiState.value.playlist ?: return
        edit("Image updated") { playlistRepository.setPlaylistImage(playlist, uri) }
    }

    /** Runs an edit, then reloads the playlist so its info shows the change. */
    private fun edit(successMessage: String, block: suspend () -> Resource<Unit>) {
        if (_uiState.value.isSaving) return
        updateUiState { copy(isSaving = true, showOptionsSheet = false) }
        viewModelScope.launch {
            val result = block()
            updateUiState { copy(isSaving = false) }
            if (result is Resource.Success) {
                getPlaylist(quiet = true)
                _uiEffect.trySend(PlaylistUiEffect.ShowSnackbar(successMessage))
            } else {
                _uiEffect.trySend(PlaylistUiEffect.ShowSnackbar(result.message ?: "Something went wrong"))
            }
        }
    }

    private fun delete() {
        if (_uiState.value.isSaving) return
        updateUiState { copy(isSaving = true) }
        viewModelScope.launch {
            val result = playlistRepository.deletePlaylist(playlistId)
            updateUiState { copy(isSaving = false, showDeleteDialog = false) }
            if (result is Resource.Success) {
                _uiEffect.trySend(PlaylistUiEffect.NavigateBack)
            } else {
                _uiEffect.trySend(PlaylistUiEffect.ShowSnackbar(result.message ?: "Couldn't delete playlist"))
            }
        }
    }

    private fun updateUiState(block: PlaylistUiState.() -> PlaylistUiState) {
        _uiState.update(block)
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}
