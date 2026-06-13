package com.android.swingmusic.settings.presentation.event

internal sealed class SettingsUiEffect {
    data object NavigateBack : SettingsUiEffect()
    data object NavigateToQrScan : SettingsUiEffect()
    data class ShowSnackBar(val message: String) : SettingsUiEffect()
}
