package com.android.swingmusic.profile.presentation.event

internal sealed class StorageUiEffect {
    data object NavigateBack : StorageUiEffect()
    data class ShowSnackBar(val message: String) : StorageUiEffect()
}
