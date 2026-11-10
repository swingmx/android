package com.android.swingmusic.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.profile.domain.ChangelogRepository
import com.android.swingmusic.profile.presentation.event.ChangelogUiEffect
import com.android.swingmusic.profile.presentation.event.ChangelogUiEvent
import com.android.swingmusic.profile.presentation.state.ChangelogUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ChangelogViewModel @Inject constructor(
    private val changelogRepository: ChangelogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangelogUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<ChangelogUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val changelog = changelogRepository.getChangelog()
            _uiState.update {
                it.copy(
                    changelog = changelog,
                    isFirstLaunch = changelogRepository.isFirstLaunch(),
                    isUnseen = changelogRepository.isUnseen(),
                )
            }
        }
    }

    fun onEvent(event: ChangelogUiEvent) {
        when (event) {
            ChangelogUiEvent.ShowAllChangesToggled -> _uiState.update {
                it.copy(showAllChanges = !it.showAllChanges)
            }

            is ChangelogUiEvent.ReleaseToggled -> _uiState.update {
                val expanded = it.expandedVersionCodes
                it.copy(
                    expandedVersionCodes = if (event.versionCode in expanded) expanded - event.versionCode
                    else expanded + event.versionCode
                )
            }

            is ChangelogUiEvent.LinkClicked -> _uiEffect.trySend(ChangelogUiEffect.OpenUrl(event.url))

            ChangelogUiEvent.Dismissed -> {
                _uiState.update { it.copy(isUnseen = false) }
                viewModelScope.launch { changelogRepository.markSeen() }
            }
        }
    }
}
