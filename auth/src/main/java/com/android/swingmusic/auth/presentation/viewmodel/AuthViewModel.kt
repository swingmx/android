package com.android.swingmusic.auth.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.tokenholder.AuthTokenHolder
import com.android.swingmusic.auth.data.util.Resource
import com.android.swingmusic.auth.domain.repository.AuthRepository
import com.android.swingmusic.auth.presentation.event.AuthUiEvent
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.LogInWithQrCode
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.LogInWithUsernameAndPassword
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnBaseUrlChange
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnPasswordChange
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.OnUsernameChange
import com.android.swingmusic.auth.presentation.event.AuthUiEvent.ResetStates
import com.android.swingmusic.auth.presentation.state.AuthState
import com.android.swingmusic.auth.presentation.state.AuthUiState
import com.android.swingmusic.auth.presentation.util.AuthError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authUiState: MutableState<AuthUiState> = mutableStateOf(AuthUiState())

    init {
        getSavedBaseUrl()
    }

    // Proceed to :home if token is not null
    fun getAccessToken(): String? {
        return AuthTokenHolder.accessToken ?: authRepository.getAccessToken()
    }

    // TODO: Call this upon navigating to :home
    fun getAuthenticatedUser() {

    }

    private fun resetUiStates() {
        authUiState.value = AuthUiState()
    }

    private fun getSavedBaseUrl() {
        val url = authRepository.getBaseUrl()
        authUiState.value = authUiState.value.copy(baseUrl = url)
    }

    fun storeInputServerUrl(url: String) {
        viewModelScope.launch {
            authRepository.storeBaseUrl(url)
        }
    }

    fun createUser(username: String, password: String, email: String, roles: List<String>) {
        viewModelScope.launch {
            val createUserResult = authRepository.createUser(
                username = username,
                password = password,
                email = email,
                roles = roles
            )

            when (createUserResult) {
                is Resource.Loading -> {}
                is Resource.Error -> {}
                is Resource.Success -> {}
            }
        }
    }

    private fun logInWithUsernameAndPassword() {
        val baseUrl = authUiState.value.baseUrl
        val username = authUiState.value.username
        val password = authUiState.value.password

        viewModelScope.launch {
            if (baseUrl.isNullOrEmpty() || !validInputUrl(baseUrl)) {
                authUiState.value = authUiState.value.copy(
                    authState = AuthState.LOGGED_OUT,
                    isLoading = false,
                    authError = AuthError.InputError("ENTER A VALID URL")
                )
                return@launch
            }

            if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                authUiState.value = authUiState.value.copy(
                    authState = AuthState.LOGGED_OUT,
                    isLoading = false,
                    authError = AuthError.LoginError(msg = "ALL INPUTS ARE REQUIRED")
                )
                return@launch
            }

            val logInResult = authRepository.logInWithUsernameAndPassword(
                baseUrl = baseUrl, username = username, password = password
            )

            when (logInResult) {
                is Resource.Loading -> {
                    authUiState.value = authUiState.value.copy(
                        authState = AuthState.LOGGED_OUT,
                        isLoading = true,
                        authError = AuthError.None
                    )
                }

                is Resource.Error -> {
                    authUiState.value = authUiState.value.copy(
                        authState = AuthState.LOGGED_OUT,
                        isLoading = false,
                        authError = AuthError.LoginError(msg = logInResult.message!!)
                    )
                }

                is Resource.Success -> {
                    val accessToken = logInResult.data!!.accessToken
                    val refreshToken = logInResult.data.refreshToken
                    val mxAge = logInResult.data.maxAge
                    val loggedInAs = logInResult.data.msg // e.g Logged in as Admin

                    authRepository.storeBaseUrl(baseUrl)
                    authRepository.storeAuthTokens(accessToken, refreshToken, loggedInAs, mxAge)

                    authUiState.value = authUiState.value.copy(
                        authState = AuthState.AUTHENTICATED,
                        isLoading = false,
                        authError = AuthError.None,
                        baseUrl = baseUrl
                    )
                }
            }
        }
    }

    private fun logInWithQrCode(encoded: String) {
        viewModelScope.launch {
            resetUiStates()

            val pair = authRepository.processQrCodeData(encoded)
            val url = pair.first
            val pairCode = pair.second

            if (url.isEmpty() or pairCode.isEmpty()) {
                authUiState.value = AuthUiState(
                    authState = AuthState.LOGGED_OUT,
                    isLoading = false,
                    authError = AuthError.LoginError("INVALID QR CODE")
                )
                return@launch
            }

            when (val qrLogInResult = authRepository.logInWithQrCode(url, pairCode)) {
                is Resource.Loading -> {
                    authUiState.value = AuthUiState(
                        authState = AuthState.LOGGED_OUT,
                        isLoading = true,
                        authError = AuthError.None
                    )
                }

                is Resource.Error -> {
                    authUiState.value = AuthUiState(
                        authState = AuthState.LOGGED_OUT,
                        isLoading = false,
                        authError = AuthError.LoginError(msg = qrLogInResult.message!!)
                    )
                }

                is Resource.Success -> {
                    val accessToken = qrLogInResult.data!!.accessToken
                    val refreshToken = qrLogInResult.data.refreshToken
                    val maxAge = qrLogInResult.data.maxAge
                    val loggedInAs = qrLogInResult.data.msg // e.g Logged in as Admin

                    authRepository.storeBaseUrl(url)
                    authRepository.storeAuthTokens(accessToken, refreshToken, loggedInAs, maxAge)

                    authUiState.value = AuthUiState(
                        authState = AuthState.AUTHENTICATED,
                        isLoading = false,
                        authError = AuthError.None,
                        baseUrl = url
                    )
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

            is ResetStates -> {
                resetUiStates()
            }

            is OnBaseUrlChange -> {
                authUiState.value = authUiState.value.copy(
                    baseUrl = event.newInput.trim(),
                    authError = AuthError.None
                )
            }

            is OnUsernameChange -> {
                authUiState.value = authUiState.value.copy(
                    username = event.newInput,
                    authError = AuthError.None
                )
            }

            is OnPasswordChange -> {
                authUiState.value = authUiState.value.copy(
                    password = event.newInput,
                    authError = AuthError.None
                )
            }

            else -> {}
        }
    }
}
