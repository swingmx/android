package com.android.swingmusic.album.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.album.domain.AlbumRepository
import com.android.swingmusic.album.presentation.event.AlbumWithInfoUiEvent
import com.android.swingmusic.album.presentation.state.AlbumWithInfoState
import com.android.swingmusic.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumWithInfoViewModel @Inject constructor(
    private val albumRepository: AlbumRepository
) : ViewModel() {

    private val _albumWithInfoState: MutableState<AlbumWithInfoState> =
        mutableStateOf(AlbumWithInfoState())
    val albumWithInfoState: State<AlbumWithInfoState> get() = _albumWithInfoState

    // TODO: handle events

    fun onAlbumWithInfoUiEvent(event: AlbumWithInfoUiEvent) {
        when (event) {
            is AlbumWithInfoUiEvent.OnLoadAlbumWithInfo -> {
                viewModelScope.launch {
                    _albumWithInfoState.value =
                        _albumWithInfoState.value.copy(albumHash = event.albumHash)

                    val result = albumRepository.getAlbumWithInfo(event.albumHash)
                    result.collectLatest {
                        _albumWithInfoState.value =
                            _albumWithInfoState.value.copy(albumWithInfo = it)
                    }
                }
            }

            is AlbumWithInfoUiEvent.OnRefreshAlbumInfo -> {
                viewModelScope.launch {
                    val result =
                        albumRepository.getAlbumWithInfo(_albumWithInfoState.value.albumHash ?: "")
                    result.collectLatest {
                        _albumWithInfoState.value =
                            _albumWithInfoState.value.copy(albumWithInfo = it)
                    }
                }
            }

            else -> {}
        }
    }
}
