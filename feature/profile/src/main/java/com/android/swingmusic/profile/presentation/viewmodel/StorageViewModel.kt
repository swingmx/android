package com.android.swingmusic.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.profile.domain.ProfileRepository
import com.android.swingmusic.profile.presentation.event.StorageUiEffect
import com.android.swingmusic.profile.presentation.event.StorageUiEvent
import com.android.swingmusic.profile.presentation.state.StorageUiState
import com.android.swingmusic.profile.presentation.util.formatBytes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StorageViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<StorageUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val size = profileRepository.getImageCacheSize()
            _uiState.update { it.copy(imageCacheBytes = size) }
        }
    }

    fun onEvent(event: StorageUiEvent) {
        when (event) {
            StorageUiEvent.OnBackClicked -> _uiEffect.trySend(StorageUiEffect.NavigateBack)
            StorageUiEvent.OnClearImageCache -> clearImageCache()
        }
    }

    private fun clearImageCache() {
        if (_uiState.value.isClearing) return
        val freed = _uiState.value.imageCacheBytes ?: 0L
        _uiState.update { it.copy(isClearing = true) }
        viewModelScope.launch {
            profileRepository.clearImageCache()
            val size = profileRepository.getImageCacheSize()
            _uiState.update { it.copy(isClearing = false, imageCacheBytes = size) }
            _uiEffect.trySend(
                StorageUiEffect.ShowSnackBar("Image cache cleared · ${formatBytes(freed - size)} freed")
            )
        }
    }
}
