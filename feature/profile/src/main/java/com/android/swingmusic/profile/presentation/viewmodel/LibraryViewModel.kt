package com.android.swingmusic.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.profile.domain.ProfileRepository
import com.android.swingmusic.profile.presentation.event.LibraryUiEffect
import com.android.swingmusic.profile.presentation.event.LibraryUiEvent
import com.android.swingmusic.profile.presentation.state.LibraryUiState
import com.android.swingmusic.profile.presentation.state.isAdmin
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
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<LibraryUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val isAdmin = profileRepository.getCachedUser()?.isAdmin == true
            updateUiState { copy(isAdmin = isAdmin) }
            if (isAdmin) loadRootDirs()
        }
    }

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            LibraryUiEvent.OnBackClicked -> _uiEffect.trySend(LibraryUiEffect.NavigateBack)
            LibraryUiEvent.OnFoldersClicked -> _uiEffect.trySend(LibraryUiEffect.NavigateToFolders)
            LibraryUiEvent.OnQuickScanClicked -> triggerScan(fullScan = false)
            LibraryUiEvent.OnFullScanClicked -> updateUiState { copy(showFullScanDialog = true) }
            LibraryUiEvent.OnFullScanDismissed -> updateUiState { copy(showFullScanDialog = false) }
            LibraryUiEvent.OnFullScanConfirmed -> {
                updateUiState { copy(showFullScanDialog = false) }
                triggerScan(fullScan = true)
            }

            LibraryUiEvent.OnRetryRootDirs -> loadRootDirs()
        }
    }

    private fun loadRootDirs() {
        updateUiState { copy(isLoadingRootDirs = true, errorLoadingRootDirs = null) }
        viewModelScope.launch {
            when (val result = profileRepository.getRootDirectories()) {
                is Resource.Success -> updateUiState {
                    copy(isLoadingRootDirs = false, rootDirs = result.data.orEmpty())
                }

                is Resource.Error -> updateUiState {
                    copy(isLoadingRootDirs = false, errorLoadingRootDirs = result.message)
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun triggerScan(fullScan: Boolean) {
        if (_uiState.value.isStartingScan) return
        updateUiState { copy(isStartingScan = true) }
        viewModelScope.launch {
            val message = when (val result = profileRepository.triggerScan(fullScan)) {
                is Resource.Success -> if (fullScan) "Full scan started" else "Quick scan started"
                else -> result.message ?: "Couldn't start the scan."
            }
            updateUiState { copy(isStartingScan = false) }
            _uiEffect.trySend(LibraryUiEffect.ShowSnackBar(message))
        }
    }

    private fun updateUiState(block: LibraryUiState.() -> LibraryUiState) {
        _uiState.update(block)
    }
}
