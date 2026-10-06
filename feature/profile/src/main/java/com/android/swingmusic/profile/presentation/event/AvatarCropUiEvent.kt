package com.android.swingmusic.profile.presentation.event

import android.graphics.Rect

internal sealed interface AvatarCropUiEvent {
    data object OnCancel : AvatarCropUiEvent
    data object OnRotate : AvatarCropUiEvent
    /** [cropRect] is in the current bitmap's pixels. */
    data class OnSave(val cropRect: Rect) : AvatarCropUiEvent
}
