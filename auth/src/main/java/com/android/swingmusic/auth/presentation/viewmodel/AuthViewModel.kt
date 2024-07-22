package com.android.swingmusic.auth.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.auth.presentation.event.AuthUiEvent
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.LogInWithQrCode
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.LogInWithUsernameAndPassword
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnBaseUrlChange
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnPasswordChange
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnUsernameChange
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.ClearErrorState
import com.android.swingmusic.auth.presentation.state.AuthState
import com.android.swingmusic.auth.presentation.state.AuthUiState
import com.android.swingmusic.auth.presentation.util.AuthError
import com.android.swingmusic.core.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isUserLoggedInFlow = MutableStateFlow(false)
    val isUserLoggedInFlow: Flow<Boolean> get() = _isUserLoggedInFlow

    private val _authUiState: MutableStateFlow<AuthUiState> = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> get() = _authUiState

    init {
        getSavedBaseUrl()
    }

    // Proceed to :home if token is not null
    fun isUserLoggedIn(): State<Boolean> {
        val token = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        return mutableStateOf(!token.isNullOrEmpty())
    }

    fun updateIsUserLoggedInFlow() {
        val token = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
        _isUserLoggedInFlow.value = !token.isNullOrEmpty()
    }

    private suspend fun getAuthenticatedUser() {
        val user = authRepository.getLoggedInUser()
        // TODO: Call this at :home, update userUi
    }

    private fun clearErrorState() {
        _authUiState.value = _authUiState.value.copy(
            authError = AuthError.None
        )
    }

    private fun getSavedBaseUrl() {
        val url = authRepository.getBaseUrl()
        _authUiState.value = _authUiState.value.copy(baseUrl = url)
    }

    fun createUser(username: String, password: String, email: String, roles: List<String>) {
        viewModelScope.launch {
            val createUserResult = authRepository.createUser(
                username = username,
                password = password,
                email = email,
                roles = roles
            )
            createUserResult.collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {}
                    is Resource.Error -> {}
                    is Resource.Success -> {}
                }
            }
        }
    }

    private fun logInWithUsernameAndPassword() {
        val baseUrl = _authUiState.value.baseUrl
        val username = _authUiState.value.username
        val password = _authUiState.value.password

        viewModelScope.launch {
            if (baseUrl.isNullOrEmpty() || !validInputUrl(baseUrl)) {
                _authUiState.value = _authUiState.value.copy(
                    authState = AuthState.LOGGED_OUT,
                    isLoading = false,
                    authError = AuthError.InputError("ENTER A VALID URL")
                )
                return@launch
            }

            if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                _authUiState.value = _authUiState.value.copy(
                    authState = AuthState.LOGGED_OUT,
                    isLoading = false,
                    authError = AuthError.LoginError(msg = "ALL INPUTS ARE REQUIRED")
                )
                return@launch
            }

            val result = authRepository.logInWithUsernameAndPassword(
                baseUrl = baseUrl,
                username = username,
                password = password
            )
            result.collectLatest { logInResult ->
                when (logInResult) {
                    is Resource.Loading -> {
                        _authUiState.value = _authUiState.value.copy(
                            authState = AuthState.LOGGED_OUT,
                            isLoading = true,
                            authError = AuthError.None
                        )
                    }

                    is Resource.Error -> {
                        _authUiState.value = _authUiState.value.copy(
                            authState = AuthState.LOGGED_OUT,
                            isLoading = false,
                            authError = AuthError.LoginError(msg = logInResult.message!!)
                        )
                    }

                    is Resource.Success -> {
                        val accessToken = logInResult.data!!.accessToken
                        val refreshToken = logInResult.data!!.refreshToken
                        val mxAge = logInResult.data!!.maxAge

                        authRepository.storeBaseUrl(baseUrl)
                        authRepository.storeAuthTokens(accessToken, refreshToken, mxAge)

                        _authUiState.value = _authUiState.value.copy(
                            authState = AuthState.AUTHENTICATED,
                            isLoading = false,
                            authError = AuthError.None,
                            baseUrl = baseUrl
                        )
                    }
                }
            }
        }
    }

    private fun logInWithQrCode(encoded: String) {
        viewModelScope.launch {
            clearErrorState()

            val pair = authRepository.processQrCodeData(encoded)
            val url = pair.first
            val pairCode = pair.second

            if (url.isEmpty() or pairCode.isEmpty()) {
                _authUiState.value = _authUiState.value.copy(
                    authState = AuthState.LOGGED_OUT,
                    isLoading = false,
                    authError = AuthError.LoginError("INVALID QR CODE")
                )
                return@launch
            }

            val result = authRepository.logInWithQrCode(url, pairCode)
            result.collectLatest { qrLogInResult ->
                when (qrLogInResult) {
                    is Resource.Loading -> {
                        _authUiState.value = _authUiState.value.copy(
                            authState = AuthState.LOGGED_OUT,
                            isLoading = true,
                            authError = AuthError.None
                        )
                    }

                    is Resource.Error -> {
                        _authUiState.value = _authUiState.value.copy(
                            authState = AuthState.LOGGED_OUT,
                            isLoading = false,
                            authError = AuthError.LoginError(msg = qrLogInResult.message!!)
                        )
                    }

                    is Resource.Success -> {
                        val accessToken = qrLogInResult.data!!.accessToken
                        val refreshToken = qrLogInResult.data!!.refreshToken
                        val maxAge = qrLogInResult.data!!.maxAge

                        authRepository.storeAuthTokens(accessToken, refreshToken, maxAge)
                        authRepository.storeBaseUrl(url)

                        _authUiState.value = _authUiState.value.copy(
                            authState = AuthState.AUTHENTICATED,
                            isLoading = false,
                            authError = AuthError.None,
                            baseUrl = url
                        )
                    }
                }
            }
        }
    }

    private fun validInputUrl(url: String?): Boolean {
        val urlRegex = Regex("^(https?|ftp)://[^\\s/$.?#].\\S*$")
        return url?.matches(urlRegex) == true
    }

    fun onAuthUiEvent(event: AuthUiEvent) {
        when (event) {
            is LogInWithQrCode -> {
                logInWithQrCode(event.encoded)
            }

            is LogInWithUsernameAndPassword -> {
                logInWithUsernameAndPassword()
            }

            is ClearErrorState -> {
                clearErrorState()
            }

            is OnBaseUrlChange -> {
                _authUiState.value = _authUiState.value.copy(
                    baseUrl = event.newInput.trim(),
                    authError = AuthError.None
                )
            }

            is OnUsernameChange -> {
                _authUiState.value = _authUiState.value.copy(
                    username = event.newInput,
                    authError = AuthError.None
                )
            }

            is OnPasswordChange -> {
                _authUiState.value = _authUiState.value.copy(
                    password = event.newInput,
                    authError = AuthError.None
                )
            }

            else -> {}
        }
    }
}
