package com.android.swingmusic.profile.presentation.event

internal sealed class LibraryUiEffect {
    data object NavigateBack : LibraryUiEffect()
    data object NavigateToFolders : LibraryUiEffect()
    data class ShowSnackBar(val message: String) : LibraryUiEffect()
}
