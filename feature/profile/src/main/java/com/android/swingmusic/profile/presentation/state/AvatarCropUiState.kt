package com.android.swingmusic.profile.presentation.state

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
internal data class AvatarCropUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Upright, decoded photo. Replaced (not mutated) on rotate. */
    val bitmap: Bitmap? = null,
    val isSaving: Boolean = false,
)
