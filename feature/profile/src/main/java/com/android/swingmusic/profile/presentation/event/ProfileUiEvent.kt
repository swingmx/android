package com.android.swingmusic.profile.presentation.event

internal sealed interface ProfileUiEvent {
    data object OnBackClicked : ProfileUiEvent
    data object OnCopyServerClicked : ProfileUiEvent
    data object OnAvatarClicked : ProfileUiEvent
    data object OnChangePhoto : ProfileUiEvent
    data object OnRemovePhoto : ProfileUiEvent
    data object OnPhotoViewerDismissed : ProfileUiEvent
    data class OnPhotoPicked(val uri: String) : ProfileUiEvent
    data object OnUndoRemovePhoto : ProfileUiEvent
    data object OnPhotoRemovalSettled : ProfileUiEvent
    data object OnRetryStats : ProfileUiEvent
    data object OnStatsClicked : ProfileUiEvent
    data object OnLibraryClicked : ProfileUiEvent
    data object OnPairDeviceClicked : ProfileUiEvent
    data object OnSettingsClicked : ProfileUiEvent
    data object OnLogOutClicked : ProfileUiEvent
    data object OnLogOutDismissed : ProfileUiEvent
    data object OnLogOutConfirmed : ProfileUiEvent
}
