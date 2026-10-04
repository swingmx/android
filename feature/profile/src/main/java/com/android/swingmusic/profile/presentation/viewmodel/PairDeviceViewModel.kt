package com.android.swingmusic.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.profile.domain.ProfileRepository
import com.android.swingmusic.profile.presentation.event.PairDeviceUiEffect
import com.android.swingmusic.profile.presentation.event.PairDeviceUiEvent
import com.android.swingmusic.profile.presentation.state.PairDeviceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PairDeviceViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairDeviceUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<PairDeviceUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val baseUrl = profileRepository.getBaseUrl()
            val username = profileRepository.getCachedUser()?.username.orEmpty()
            updateUiState { copy(baseUrl = baseUrl, username = username) }
        }
        loadCode()
    }

    fun onEvent(event: PairDeviceUiEvent) {
        when (event) {
            PairDeviceUiEvent.OnBackClicked -> _uiEffect.trySend(PairDeviceUiEffect.NavigateBack)
            PairDeviceUiEvent.OnNewCodeClicked -> loadCode()
        }
    }

    private fun loadCode() {
        updateUiState { copy(isLoadingCode = true, errorLoadingCode = null) }
        viewModelScope.launch {
            when (val result = profileRepository.getPairCode()) {
                is Resource.Success -> updateUiState {
                    copy(isLoadingCode = false, code = result.data)
                }

                is Resource.Error -> updateUiState {
                    copy(isLoadingCode = false, code = null, errorLoadingCode = result.message)
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun updateUiState(block: PairDeviceUiState.() -> PairDeviceUiState) {
        _uiState.update(block)
    }
}
