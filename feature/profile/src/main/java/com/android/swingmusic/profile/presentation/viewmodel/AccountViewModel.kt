package com.android.swingmusic.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.profile.domain.ProfileRepository
import com.android.swingmusic.profile.presentation.event.AccountUiEffect
import com.android.swingmusic.profile.presentation.event.AccountUiEvent
import com.android.swingmusic.profile.presentation.state.AccountUiState
import com.android.swingmusic.profile.presentation.state.canSaveUsername
import com.android.swingmusic.profile.presentation.state.canUpdatePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AccountViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<AccountUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val username = profileRepository.getCachedUser()?.username.orEmpty()
            updateUiState { copy(currentUsername = username, username = username) }
        }
    }

    fun onEvent(event: AccountUiEvent) {
        when (event) {
            AccountUiEvent.OnBackClicked -> _uiEffect.trySend(AccountUiEffect.NavigateBack)
            is AccountUiEvent.OnUsernameChange -> updateUiState {
                copy(username = event.value, usernameError = null)
            }

            AccountUiEvent.OnSaveUsername -> saveUsername()
            is AccountUiEvent.OnNewPasswordChange -> updateUiState {
                copy(newPassword = event.value, passwordError = null)
            }

            is AccountUiEvent.OnConfirmPasswordChange -> updateUiState {
                copy(confirmPassword = event.value, passwordError = null)
            }

            AccountUiEvent.OnUpdatePassword -> updatePassword()
        }
    }

    private fun saveUsername() {
        val state = _uiState.value
        if (!state.canSaveUsername) return
        val username = state.username.trim()

        updateUiState { copy(isSavingUsername = true, usernameError = null) }
        viewModelScope.launch {
            when (val result = profileRepository.updateProfile(username = username)) {
                is Resource.Success -> {
                    val saved = result.data?.username ?: username
                    updateUiState {
                        copy(isSavingUsername = false, currentUsername = saved, username = saved)
                    }
                    _uiEffect.trySend(AccountUiEffect.ShowSnackBar("Username updated"))
                }

                is Resource.Error -> updateUiState {
                    copy(isSavingUsername = false, usernameError = result.message)
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun updatePassword() {
        val state = _uiState.value
        if (!state.canUpdatePassword) return

        updateUiState { copy(isSavingPassword = true, passwordError = null) }
        viewModelScope.launch {
            when (val result = profileRepository.updateProfile(password = state.newPassword)) {
                is Resource.Success -> {
                    updateUiState {
                        copy(isSavingPassword = false, newPassword = "", confirmPassword = "")
                    }
                    _uiEffect.trySend(AccountUiEffect.ShowSnackBar("Password updated"))
                }

                is Resource.Error -> updateUiState {
                    copy(isSavingPassword = false, passwordError = result.message)
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun updateUiState(block: AccountUiState.() -> AccountUiState) {
        _uiState.update(block)
    }
}
