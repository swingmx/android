package com.android.swingmusic.library.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.HomeItem
import com.android.swingmusic.core.domain.model.RecentFavourite
import com.android.swingmusic.uicomponent.R

private val CardWidth = 136.dp

/** A recently favourited album, artist or track, captioned with when it was favourited. */
@Composable
internal fun RecentFavouriteCard(
    recent: RecentFavourite,
    baseUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = recent.item
    val isArtist = item is HomeItem.ArtistItem
    val (url, fallback) = when (item) {
        is HomeItem.AlbumItem -> "${baseUrl}img/thumbnail/medium/${item.album.image}" to R.drawable.audio_fallback
        is HomeItem.ArtistItem -> "${baseUrl}img/artist/medium/${item.artist.image}" to R.drawable.artist_fallback
        is HomeItem.TrackItem -> "${baseUrl}img/thumbnail/medium/${item.track.image}" to R.drawable.audio_fallback
        else -> "" to R.drawable.audio_fallback
    }
    val title = when (item) {
        is HomeItem.AlbumItem -> item.album.title
        is HomeItem.ArtistItem -> item.artist.name
        is HomeItem.TrackItem -> item.track.title
        is HomeItem.PlaylistItem -> item.playlist.name
        else -> ""
    }
    val kind = when (item) {
        is HomeItem.AlbumItem -> "Album"
        is HomeItem.ArtistItem -> "Artist"
        is HomeItem.TrackItem -> "Track"
        is HomeItem.PlaylistItem -> "Playlist"
        else -> ""
    }

    Column(
        modifier = modifier
            .width(CardWidth)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = if (isArtist) Alignment.CenterHorizontally else Alignment.Start
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            placeholder = painterResource(fallback),
            fallback = painterResource(fallback),
            error = painterResource(fallback),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(CardWidth)
                .clip(if (isArtist) CircleShape else RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = listOf(kind, recent.time).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
