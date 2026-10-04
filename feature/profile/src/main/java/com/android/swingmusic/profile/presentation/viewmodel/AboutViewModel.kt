package com.android.swingmusic.profile.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.profile.domain.ProfileRepository
import com.android.swingmusic.profile.presentation.event.AboutUiEffect
import com.android.swingmusic.profile.presentation.event.AboutUiEvent
import com.android.swingmusic.profile.presentation.state.AboutUiState
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
internal class AboutViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AboutUiState(appVersion = context.appVersion()))
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<AboutUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val version = profileRepository.getServerVersion()
            _uiState.update { it.copy(serverVersion = version) }
        }
    }

    fun onEvent(event: AboutUiEvent) {
        when (event) {
            AboutUiEvent.OnBackClicked -> _uiEffect.trySend(AboutUiEffect.NavigateBack)
            AboutUiEvent.OnReportProblemClicked -> _uiEffect.trySend(AboutUiEffect.OpenUrl(issueUrl()))
            AboutUiEvent.OnSourceCodeClicked -> _uiEffect.trySend(AboutUiEffect.OpenUrl(REPO_URL))
        }
    }

    private fun issueUrl(): String {
        val state = _uiState.value
        val body = buildString {
            append("**What happened?**\n\n\n**Steps to reproduce**\n\n\n---\n")
            append("App: ${state.appVersion}\n")
            append("Server: ${state.serverVersion ?: "unknown"}\n")
            append("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), ${Build.MANUFACTURER} ${Build.MODEL}")
        }
        return "$REPO_URL/issues/new?body=${Uri.encode(body)}"
    }

    private companion object {
        const val REPO_URL = "https://github.com/swingmx/android"
    }
}
