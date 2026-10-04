package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class PairDeviceUiState(
    val baseUrl: String = "",
    val username: String = "",

    val isLoadingCode: Boolean = true,
    val errorLoadingCode: String? = null,
    val code: String? = null,
)

/** Same "<server url> <code>" format the web client encodes and our QR login parses. */
internal val PairDeviceUiState.qrPayload: String?
    get() = code?.let { "${baseUrl.trimEnd('/')} $it" }
