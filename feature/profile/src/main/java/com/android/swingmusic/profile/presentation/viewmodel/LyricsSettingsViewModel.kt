package com.android.swingmusic.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.profile.presentation.event.LyricsSettingsUiEffect
import com.android.swingmusic.profile.presentation.event.LyricsSettingsUiEvent
import com.android.swingmusic.profile.presentation.state.LyricsSettingsUiState
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LyricsSettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val uiState = combine(
        appSettingsRepository.useLyricsPlugin,
        appSettingsRepository.lyricsAutoDownload,
        appSettingsRepository.lyricsOverrideUnsynced,
        appSettingsRepository.lyricsWordSweep
    ) { usePlugin, autoDownload, preferSynced, wordSweep ->
        LyricsSettingsUiState(usePlugin, autoDownload, preferSynced, wordSweep)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LyricsSettingsUiState())

    private val _uiEffect = Channel<LyricsSettingsUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: LyricsSettingsUiEvent) {
        when (event) {
            LyricsSettingsUiEvent.OnBackClicked -> _uiEffect.trySend(LyricsSettingsUiEffect.NavigateBack)
            is LyricsSettingsUiEvent.OnUsePluginChange -> viewModelScope.launch {
                appSettingsRepository.setUseLyricsPlugin(event.enabled)
            }

            is LyricsSettingsUiEvent.OnAutoDownloadChange -> viewModelScope.launch {
                appSettingsRepository.setLyricsAutoDownload(event.enabled)
            }

            is LyricsSettingsUiEvent.OnPreferSyncedChange -> viewModelScope.launch {
                appSettingsRepository.setLyricsOverrideUnsynced(event.enabled)
            }

            is LyricsSettingsUiEvent.OnWordSweepChange -> viewModelScope.launch {
                appSettingsRepository.setLyricsWordSweep(event.enabled)
            }
        }
    }
}
