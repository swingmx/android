package com.android.swingmusic.playlist.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.playlist.domain.Collection

@Immutable
internal data class AddToCollectionUiState(
    val isLoadingCollections: Boolean = true,
    val errorLoadingCollections: String? = null,
    val collections: List<Collection> = emptyList(),
    val showNewCollectionField: Boolean = false,
    val newCollectionName: String = "",
    val newCollectionError: String? = null,
    /** True while an add or create request is running, so taps are ignored. */
    val isSaving: Boolean = false,
    val errorAddingToCollection: String? = null,
)
