package com.android.swingmusic.profile.presentation.event

internal sealed class AvatarCropUiEffect {
    data object NavigateBack : AvatarCropUiEffect()
    data class ShowMessage(val message: String) : AvatarCropUiEffect()
}
