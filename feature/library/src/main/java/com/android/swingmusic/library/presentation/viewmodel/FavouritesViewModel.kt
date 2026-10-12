package com.android.swingmusic.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.avatar.AvatarStore
import com.android.swingmusic.library.presentation.event.FavouritesUiEffect
import com.android.swingmusic.library.presentation.event.FavouritesUiEvent
import com.android.swingmusic.library.presentation.state.FavouritesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FavouritesViewModel @Inject constructor(
    private val avatarStore: AvatarStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavouritesUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<FavouritesUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            avatarStore.avatar.collect { file -> updateUiState { copy(avatarPath = file?.absolutePath) } }
        }
    }

    fun onEvent(event: FavouritesUiEvent) {
        when (event) {
            FavouritesUiEvent.OnProfileClicked -> _uiEffect.trySend(FavouritesUiEffect.NavigateToProfile)
        }
    }

    private fun updateUiState(block: FavouritesUiState.() -> FavouritesUiState) {
        _uiState.update(block)
    }
}
