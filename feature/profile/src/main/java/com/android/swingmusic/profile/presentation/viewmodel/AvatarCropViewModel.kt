package com.android.swingmusic.profile.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.auth.data.avatar.AvatarStore
import com.android.swingmusic.profile.data.avatar.AvatarImages
import com.android.swingmusic.profile.presentation.event.AvatarCropUiEffect
import com.android.swingmusic.profile.presentation.event.AvatarCropUiEvent
import com.android.swingmusic.profile.presentation.state.AvatarCropUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
internal class AvatarCropViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val avatarStore: AvatarStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val imageUri: String = savedStateHandle.get<String>("imageUri").orEmpty()

    private val _uiState = MutableStateFlow(AvatarCropUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<AvatarCropUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                imageUri.takeIf { it.isNotEmpty() }?.let { AvatarImages.decode(context, Uri.parse(it)) }
            }
            _uiState.update {
                if (bitmap == null) it.copy(isLoading = false, error = "Couldn't open that photo.")
                else it.copy(isLoading = false, bitmap = bitmap)
            }
        }
    }

    fun onEvent(event: AvatarCropUiEvent) {
        when (event) {
            AvatarCropUiEvent.OnCancel -> _uiEffect.trySend(AvatarCropUiEffect.NavigateBack)
            AvatarCropUiEvent.OnRotate -> rotate()
            is AvatarCropUiEvent.OnSave -> save(event)
        }
    }

    private fun rotate() {
        val bitmap = _uiState.value.bitmap ?: return
        viewModelScope.launch {
            val rotated = withContext(Dispatchers.Default) { AvatarImages.rotate(bitmap, -90F) }
            _uiState.update { it.copy(bitmap = rotated) }
        }
    }

    private fun save(event: AvatarCropUiEvent.OnSave) {
        val state = _uiState.value
        val bitmap = state.bitmap ?: return
        if (state.isSaving) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val avatar = withContext(Dispatchers.Default) { AvatarImages.crop(bitmap, event.cropRect) }
            if (avatarStore.save(avatar)) {
                _uiEffect.trySend(AvatarCropUiEffect.NavigateBack)
            } else {
                _uiState.update { it.copy(isSaving = false, error = "Couldn't save your photo.") }
            }
        }
    }
}
