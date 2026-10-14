package com.android.swingmusic.profile.presentation.event

internal sealed class LibraryManagementUiEffect {
    data object NavigateBack : LibraryManagementUiEffect()
    data class ShowSnackBar(val message: String) : LibraryManagementUiEffect()
}
