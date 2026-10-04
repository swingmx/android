package com.android.swingmusic.profile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.profile.domain.ProfileRepository
import com.android.swingmusic.profile.presentation.event.ProfileUiEffect
import com.android.swingmusic.profile.presentation.event.ProfileUiEvent
import com.android.swingmusic.profile.presentation.state.ProfileUiState
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
internal class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(appVersion = context.appVersion()))
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadUser()
        loadStats()
        loadServerVersion()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.OnBackClicked -> _uiEffect.trySend(ProfileUiEffect.NavigateBack)
            ProfileUiEvent.OnCopyServerClicked -> {
                val url = _uiState.value.baseUrl.trimEnd('/')
                if (url.isNotEmpty()) _uiEffect.trySend(ProfileUiEffect.CopyToClipboard(url))
            }

            ProfileUiEvent.OnRetryStats -> loadStats()
            ProfileUiEvent.OnStatsClicked -> _uiEffect.trySend(ProfileUiEffect.NavigateToStats)
            ProfileUiEvent.OnLibraryClicked -> _uiEffect.trySend(ProfileUiEffect.NavigateToLibrary)
            ProfileUiEvent.OnPairDeviceClicked -> _uiEffect.trySend(ProfileUiEffect.NavigateToPairDevice)
            ProfileUiEvent.OnSettingsClicked -> _uiEffect.trySend(ProfileUiEffect.NavigateToSettings)
            ProfileUiEvent.OnLogOutClicked -> updateUiState { copy(showLogOutDialog = true) }
            ProfileUiEvent.OnLogOutDismissed -> {
                if (!_uiState.value.isLoggingOut) updateUiState { copy(showLogOutDialog = false) }
            }

            ProfileUiEvent.OnLogOutConfirmed -> logOut()
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            val baseUrl = profileRepository.getBaseUrl()
            val cached = profileRepository.getCachedUser()
            updateUiState { copy(baseUrl = baseUrl, user = cached) }

            profileRepository.fetchUser().data?.let { user ->
                updateUiState { copy(user = user) }
            }
        }
    }

    private fun loadStats() {
        updateUiState { copy(isLoadingStats = true, errorLoadingStats = null) }
        viewModelScope.launch {
            when (val result = profileRepository.getWeeklyStats()) {
                is Resource.Success -> updateUiState {
                    copy(isLoadingStats = false, stats = result.data.orEmpty())
                }

                is Resource.Error -> updateUiState {
                    copy(isLoadingStats = false, errorLoadingStats = result.message)
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadServerVersion() {
        viewModelScope.launch {
            val version = profileRepository.getServerVersion()
            updateUiState { copy(serverVersion = version) }
        }
    }

    private fun logOut() {
        if (_uiState.value.isLoggingOut) return
        updateUiState { copy(isLoggingOut = true) }
        // The app shell reacts to the session ending and returns to the login screen.
        viewModelScope.launch { profileRepository.logOut() }
    }

    private fun updateUiState(block: ProfileUiState.() -> ProfileUiState) {
        _uiState.update(block)
    }
}
