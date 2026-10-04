package com.android.swingmusic.profile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.profile.domain.ProfileRepository
import com.android.swingmusic.profile.presentation.event.SettingsUiEffect
import com.android.swingmusic.profile.presentation.event.SettingsUiEvent
import com.android.swingmusic.profile.presentation.state.SettingsUiState
import com.android.swingmusic.profile.presentation.util.appVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(appVersion = context.appVersion()))
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<SettingsUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadCacheSize()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            SettingsUiEvent.OnBackClicked -> _uiEffect.trySend(SettingsUiEffect.NavigateBack)
            SettingsUiEvent.OnAccountClicked -> _uiEffect.trySend(SettingsUiEffect.NavigateToAccount)
            SettingsUiEvent.OnLyricsClicked -> _uiEffect.trySend(SettingsUiEffect.NavigateToLyrics)
            SettingsUiEvent.OnStorageClicked -> _uiEffect.trySend(SettingsUiEffect.NavigateToStorage)
            SettingsUiEvent.OnAboutClicked -> _uiEffect.trySend(SettingsUiEffect.NavigateToAbout)
            SettingsUiEvent.OnScreenResumed -> loadCacheSize()
        }
    }

    private fun loadCacheSize() {
        viewModelScope.launch {
            val size = profileRepository.getImageCacheSize()
            _uiState.update { it.copy(imageCacheBytes = size) }
        }
    }
}
