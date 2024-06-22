package com.android.swingmusic.uicomponent.presentation.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

@Composable
fun createImageRequestWithAuth(
    imageUrl: String,
    accessToken: String,
    crossfade: Boolean = false,
): ImageRequest {
    val context = LocalContext.current
    return ImageRequest.Builder(context)
        .data(imageUrl)
        .addHeader("Authorization", "Bearer $accessToken")
        .crossfade(crossfade)
        .build()
}
