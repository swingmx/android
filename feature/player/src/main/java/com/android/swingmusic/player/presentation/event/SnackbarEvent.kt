package com.android.swingmusic.player.presentation.event

import androidx.compose.material3.SnackbarDuration

data class SnackbarEvent(
    val message: String,
    val actionLabel: String = "OK",
    val duration: SnackbarDuration = SnackbarDuration.Short
)
