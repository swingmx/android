package com.android.swingmusic.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.util.Resource
import com.android.swingmusic.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {


    // TODO: Add AuthState ->

    init {
        getBaseUrl()
    }

    private fun getBaseUrl() {
        runBlocking(Dispatchers.IO) {
            // baseUrl.value = authRepository.getBaseUrl()
        }
    }

    fun getServerUrl() {
        viewModelScope.launch {
            val url = authRepository.getBaseUrl()
        }
    }

    fun storeServerUrl(url: String) {
        viewModelScope.launch {
            authRepository.storeBaseUrl(url)
        }
    }

    fun storeAuthTokens(accessToken: String, refreshToken: String) {
        runBlocking(Dispatchers.IO) {
            authRepository.storeAuthTokens(accessToken, refreshToken)
        }
    }

    fun createUser(username: String, password: String) {
        viewModelScope.launch {
            val createUserResult = authRepository.createUser(username, password)

            when (createUserResult) {
                is Resource.Loading -> {}
                is Resource.Error -> {}
                is Resource.Success -> {}
            }
        }
    }

    fun logInWithUsernameAndPassword(username: String, password: String) {
        val logInResult = authRepository.logInWithUsernameAndPassword(username, password)

        when (logInResult) {
            is Resource.Loading -> {}
            is Resource.Error -> {}
            is Resource.Success -> {}
        }
    }

    fun processQrCodeData(encoded: String) {
        val pair = authRepository.processQrCodeData(encoded)

        val url = pair.first
        val pairCode = pair.second

        logInWithQrCode(url, pairCode)
    }

    private fun logInWithQrCode(url: String, pairCode: String) {
        val qrLogInResult = authRepository.logInWithQrCode(url, pairCode)

        when (qrLogInResult) {
            is Resource.Loading -> {}
            is Resource.Error -> {}
            is Resource.Success -> {}
        }
    }

    fun initiateQrCodeScanner() {

    }
}
