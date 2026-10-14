package com.android.swingmusic.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.uicomponent.R

/** Server thumbnail size for mosaic tiles; pick by how big each tile is shown. */
internal enum class MosaicTileSize(val path: String) { SMALL("small/"), MEDIUM("medium/") }

/** The playlist's own image, or a mosaic of its first four track covers. */
@Composable
internal fun PlaylistArtwork(
    playlist: Playlist,
    baseUrl: String,
    shape: Shape,
    modifier: Modifier = Modifier,
    tileSize: MosaicTileSize = MosaicTileSize.SMALL,
) {
    val customImage = playlist.customImage
    val box = modifier
        .aspectRatio(1F)
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)

    when {
        customImage != null -> Cover(url = "${baseUrl}img/playlist/$customImage", modifier = box)

        playlist.images.isEmpty() -> Cover(url = null, modifier = box)

        playlist.images.size < 4 -> Cover(url = "${baseUrl}img/thumbnail/${playlist.images.first()}", modifier = box)

        else -> Column(modifier = box, verticalArrangement = Arrangement.spacedBy(1.dp)) {
            playlist.images.take(4).chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.weight(1F),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    row.forEach { image ->
                        Cover(
                            url = "${baseUrl}img/thumbnail/${tileSize.path}$image",
                            modifier = Modifier
                                .weight(1F)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cover(url: String?, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.audio_fallback),
        fallback = painterResource(R.drawable.audio_fallback),
        error = painterResource(R.drawable.audio_fallback),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxWidth()
    )
}
