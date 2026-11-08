package com.android.swingmusic.playlist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.playlist.domain.Collection
import com.android.swingmusic.playlist.domain.CollectionRepository
import com.android.swingmusic.playlist.domain.CollectionTarget
import com.android.swingmusic.playlist.presentation.event.AddToCollectionUiEffect
import com.android.swingmusic.playlist.presentation.event.AddToCollectionUiEvent
import com.android.swingmusic.playlist.presentation.state.AddToCollectionUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AddToCollectionViewModel.Factory::class)
internal class AddToCollectionViewModel @AssistedInject constructor(
    @Assisted private val target: CollectionTarget,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(target: CollectionTarget): AddToCollectionViewModel
    }

    private val _uiState = MutableStateFlow(AddToCollectionUiState(newCollectionName = target.suggestedName))
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<AddToCollectionUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        getCollections()
    }

    fun onEvent(event: AddToCollectionUiEvent) {
        when (event) {
            AddToCollectionUiEvent.OnRetry -> getCollections()
            is AddToCollectionUiEvent.OnCollectionClicked -> addToCollection(event.collection)
            AddToCollectionUiEvent.OnNewCollectionClicked -> updateUiState { copy(showNewCollectionField = true) }
            AddToCollectionUiEvent.OnCloseNewCollection -> updateUiState {
                copy(
                    showNewCollectionField = false,
                    newCollectionName = target.suggestedName,
                    newCollectionError = null
                )
            }

            is AddToCollectionUiEvent.OnNewCollectionNameChanged -> updateUiState {
                copy(newCollectionName = event.name, newCollectionError = null)
            }

            AddToCollectionUiEvent.OnCreateCollection -> createCollection()
        }
    }

    private fun getCollections() {
        updateUiState { copy(isLoadingCollections = true, errorLoadingCollections = null) }
        viewModelScope.launch {
            when (val result = collectionRepository.getCollections()) {
                is Resource.Success -> updateUiState {
                    copy(isLoadingCollections = false, collections = result.data.orEmpty())
                }

                else -> updateUiState {
                    copy(isLoadingCollections = false, errorLoadingCollections = result.message)
                }
            }
        }
    }

    private fun addToCollection(collection: Collection) {
        if (_uiState.value.isSaving) return
        updateUiState { copy(isSaving = true, errorAddingToCollection = null) }
        viewModelScope.launch {
            val result = collectionRepository.addToCollection(collection.id, target)
            updateUiState { copy(isSaving = false, errorAddingToCollection = result.message) }
            if (result is Resource.Success) {
                _uiEffect.trySend(AddToCollectionUiEffect.Done("Added to ${collection.name}"))
            }
        }
    }

    private fun createCollection() {
        val name = _uiState.value.newCollectionName.trim()
        if (_uiState.value.isSaving) return
        if (name.isEmpty()) {
            updateUiState { copy(newCollectionError = "Enter a name") }
            return
        }
        updateUiState { copy(isSaving = true) }
        viewModelScope.launch {
            val result = collectionRepository.createCollection(name, target)
            if (result is Resource.Success) {
                updateUiState { copy(isSaving = false) }
                _uiEffect.trySend(AddToCollectionUiEffect.Done("Created $name"))
            } else {
                updateUiState { copy(isSaving = false, newCollectionError = result.message) }
            }
        }
    }

    private fun updateUiState(block: AddToCollectionUiState.() -> AddToCollectionUiState) {
        _uiState.update(block)
    }
}
