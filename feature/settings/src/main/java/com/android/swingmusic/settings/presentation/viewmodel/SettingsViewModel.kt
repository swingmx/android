package com.android.swingmusic.settings.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import com.android.swingmusic.settings.presentation.event.SettingsUiEffect
import com.android.swingmusic.settings.presentation.event.SettingsUiEvent
import com.android.swingmusic.settings.presentation.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appSettingsRepository: AppSettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<SettingsUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadServerUrl()
        observeLyricsPrefs()
        loadAppVersion()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            SettingsUiEvent.OnBackPressed -> {
                _uiEffect.trySend(SettingsUiEffect.NavigateBack)
            }

            SettingsUiEvent.OnClickRePairDevice -> signOutAndReturnToQrScan()

            is SettingsUiEvent.OnToggleUseLyricsPlugin -> updatePref {
                appSettingsRepository.setUseLyricsPlugin(event.enabled)
            }

            is SettingsUiEvent.OnToggleLyricsAutoDownload -> updatePref {
                appSettingsRepository.setLyricsAutoDownload(event.enabled)
            }

            is SettingsUiEvent.OnToggleLyricsOverrideUnsynced -> updatePref {
                appSettingsRepository.setLyricsOverrideUnsynced(event.enabled)
            }
        }
    }

    private fun loadServerUrl() {
        viewModelScope.launch {
            val serverUrl = runCatching { authRepository.getBaseUrl().orEmpty() }
                .onFailure { Timber.tag("SETTINGS").e(it) }
                .getOrDefault("")
            _uiState.update { it.copy(serverUrl = serverUrl) }
        }
    }

    private fun observeLyricsPrefs() {
        viewModelScope.launch {
            val useLyricsPlugin = appSettingsRepository.useLyricsPlugin.first()
            val autoDownload = appSettingsRepository.lyricsAutoDownload.first()
            val overrideUnsynced = appSettingsRepository.lyricsOverrideUnsynced.first()
            _uiState.update {
                it.copy(
                    useLyricsPlugin = useLyricsPlugin,
                    lyricsAutoDownload = autoDownload,
                    lyricsOverrideUnsynced = overrideUnsynced
                )
            }
        }
    }

    private fun loadAppVersion() {
        val version = runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName.orEmpty()
        }.getOrDefault("")
        _uiState.update { it.copy(appVersion = version) }
    }

    private fun updatePref(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { Timber.tag("SETTINGS").e(it) }
            observeLyricsPrefs()
        }
    }

    private fun signOutAndReturnToQrScan() {
        if (_uiState.value.isSigningOut) return
        _uiState.update { it.copy(isSigningOut = true, signOutError = null) }
        viewModelScope.launch {
            val result = runCatching { authRepository.signOut() }
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSigningOut = false) }
                    _uiEffect.trySend(SettingsUiEffect.NavigateToQrScan)
                },
                onFailure = { error ->
                    Timber.tag("SETTINGS").e(error, "signOut failed")
                    _uiState.update {
                        it.copy(
                            isSigningOut = false,
                            signOutError = error.message ?: "Failed to sign out"
                        )
                    }
                    _uiEffect.trySend(
                        SettingsUiEffect.ShowSnackBar(
                            error.message ?: "Failed to sign out"
                        )
                    )
                }
            )
        }
    }
}
