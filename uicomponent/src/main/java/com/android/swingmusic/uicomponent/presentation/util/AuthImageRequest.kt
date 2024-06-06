package com.android.swingmusic.uicomponent.presentation.util

import android.content.Context
import coil.request.ImageRequest

fun createImageRequestWithAuth(
    imageUrl: String,
    accessToken: String,
    context: Context
): ImageRequest {
    return ImageRequest.Builder(context)
        .data(imageUrl)
        .addHeader("Authorization", "Bearer $accessToken")
        .build()
}
