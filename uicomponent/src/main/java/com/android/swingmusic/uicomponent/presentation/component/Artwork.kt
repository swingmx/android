package com.android.swingmusic.uicomponent.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.util.rememberBlurhashPainter

/**
 * Album, track or artist artwork. While the image loads, the item's blurhash is shown in
 * its place; items without one show [fallback], which also shows if the image fails.
 */
@Composable
fun Artwork(
    url: String,
    blurhash: String?,
    modifier: Modifier = Modifier,
    @DrawableRes fallback: Int = R.drawable.audio_fallback,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
) {
    val fallbackPainter = painterResource(fallback)
    val blurPainter = rememberBlurhashPainter(blurhash)
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        placeholder = blurPainter ?: fallbackPainter,
        fallback = fallbackPainter,
        error = fallbackPainter,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}
