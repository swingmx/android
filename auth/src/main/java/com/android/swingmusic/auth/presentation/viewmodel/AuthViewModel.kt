package com.android.swingmusic.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.auth.presentation.event.AuthUiEvent
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.ClearErrorState
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.LogInWithQrCode
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.LogInWithUsernameAndPassword
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnBaseUrlChange
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnPasswordChange
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnUsernameChange
import com.android.swingmusic.auth.presentation.state.AuthState
import com.android.swingmusic.auth.presentation.state.AuthUiState
import com.android.swingmusic.auth.presentation.util.AuthError
import com.android.swingmusic.auth.presentation.util.AuthUtils.normalizeUrl
import com.android.swingmusic.auth.presentation.util.AuthUtils.validInputUrl
import com.android.swingmusic.core.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authUiState: MutableStateFlow<AuthUiState> = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> get() = _authUiState


    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn.asStateFlow()

    private val _authStateEvent = Channel<AuthState>(Channel.BUFFERED)
    val authStateEvent = _authStateEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            authRepository.initializeBaseUrlAndAuthTokens()
        }
        refreshLoginState()
        refreshBaseUrlBaseUrl()
    }

    private fun refreshLoginState() {
        viewModelScope.launch {
            val token = AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
            _isUserLoggedIn.update { !token.isNullOrEmpty() }
        }
    }

    private fun refreshBaseUrlBaseUrl() {
        viewModelScope.launch {
            val url = authRepository.getBaseUrl()
            _authUiState.update { it.copy(baseUrl = url) }
        }
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
        val inputBaseUrl = _authUiState.value.baseUrl
        val baseUrl = normalizeUrl(inputBaseUrl)
        val username = _authUiState.value.username
        val password = _authUiState.value.password

        viewModelScope.launch {
            // Persist the normalized URL back to UI state so the user sees the auto-prepended scheme
            _authUiState.value = _authUiState.value.copy(baseUrl = baseUrl)

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

                        refreshLoginState()
                        refreshBaseUrlBaseUrl()

                        _authUiState.value = _authUiState.value.copy(
                            authState = AuthState.AUTHENTICATED,
                            isLoading = false,
                            authError = AuthError.None,
                            baseUrl = baseUrl
                        )

                        _authStateEvent.trySend(AuthState.AUTHENTICATED)
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

            if (url.isEmpty() || pairCode.isEmpty()) {
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

                        refreshLoginState()
                        refreshBaseUrlBaseUrl()

                        _authUiState.value = _authUiState.value.copy(
                            authState = AuthState.AUTHENTICATED,
                            isLoading = false,
                            authError = AuthError.None,
                            baseUrl = url
                        )

                        _authStateEvent.trySend(AuthState.AUTHENTICATED)
                    }
                }
            }
        }
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
