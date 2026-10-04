package com.android.swingmusic.home.presentation.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.HomeItem
import com.android.swingmusic.core.domain.model.Mix
import com.android.swingmusic.core.domain.model.MixImage
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.SoundSignalBars
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

internal val HomeCardWidth = 140.dp
private val ArtworkShape = RoundedCornerShape(12.dp)

@Composable
internal fun HomeCard(
    item: HomeItem,
    baseUrl: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    nowPlayingState: PlaybackState? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96F else 1F,
        animationSpec = spring(dampingRatio = 0.7F, stiffness = Spring.StiffnessMediumLow),
        label = "homeCardPress"
    )
    val isArtist = item is HomeItem.ArtistItem
    val shape = if (isArtist) CircleShape else ArtworkShape

    Column(
        modifier = modifier
            .width(HomeCardWidth)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                alpha = if (isPressed) 0.85F else 1F
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        horizontalAlignment = if (isArtist) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Box(modifier = Modifier.size(HomeCardWidth)) {
            when (item) {
                is HomeItem.ArtistItem -> Artwork(
                    url = "${baseUrl}img/artist/medium/${item.artist.image}",
                    shape = CircleShape,
                    fallback = R.drawable.artist_fallback
                )

                is HomeItem.AlbumItem -> Artwork(
                    url = "${baseUrl}img/thumbnail/medium/${item.album.image}",
                    shape = ArtworkShape
                )

                is HomeItem.TrackItem -> {
                    Artwork(
                        url = "${baseUrl}img/thumbnail/medium/${item.track.image}",
                        shape = ArtworkShape
                    )
                    if (nowPlayingState != null) {
                        NowPlayingBadge(
                            playbackState = nowPlayingState,
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    } else {
                        PlayBadge(modifier = Modifier.align(Alignment.BottomEnd))
                    }
                }

                is HomeItem.PlaylistItem -> PlaylistArtwork(item.playlist, baseUrl)
                is HomeItem.MixItem -> MixArtwork(item.mix, baseUrl)
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(Color.Black.copy(alpha = 0.5F)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val (title, subtitle) = item.captions()
        if (item is HomeItem.MixItem) {
            Text(
                text = subtitle,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = if (isArtist) TextAlign.Center else TextAlign.Start,
                fontSize = 14.sp,
                fontWeight = if (isArtist) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = if (isArtist) TextAlign.Center else TextAlign.Start,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun HomeItem.captions(): Pair<String, String> = when (this) {
    is HomeItem.ArtistItem -> artist.name to "Artist"
    is HomeItem.AlbumItem -> album.title to (album.albumArtists.firstOrNull()?.name ?: "Unknown Artist")
    is HomeItem.TrackItem -> track.title to
            "Track · ${track.trackArtists.firstOrNull()?.name ?: "Unknown Artist"}"

    is HomeItem.PlaylistItem -> playlist.name to
            if (playlist.trackCount == 1) "1 song" else "${playlist.trackCount} songs"

    is HomeItem.MixItem -> mix.title to mix.description.ifBlank { mix.title }
}

@Composable
private fun Artwork(
    url: String,
    shape: Shape,
    modifier: Modifier = Modifier,
    size: Dp = HomeCardWidth,
    fallback: Int = R.drawable.audio_fallback,
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
        modifier = modifier
            .size(size)
            .clip(shape)
    )
}

@Composable
private fun PlayBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6F)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.play_arrow),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun NowPlayingBadge(playbackState: PlaybackState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6F)),
        contentAlignment = Alignment.Center
    ) {
        if (playbackState == PlaybackState.ERROR) {
            Icon(
                painter = painterResource(R.drawable.error),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        } else {
            // SoundSignalBars draws fixed 32dp bars, so scale them down to fit the badge
            Box(
                modifier = Modifier
                    .requiredSize(width = 36.dp, height = 32.dp)
                    .graphicsLayer {
                        scaleX = 0.5F
                        scaleY = 0.5F
                    }
            ) {
                SoundSignalBars(animate = playbackState == PlaybackState.PLAYING)
            }
        }
    }
}

@Composable
private fun PlaylistArtwork(playlist: Playlist, baseUrl: String) {
    val customImage = playlist.customImage
    when {
        customImage != null -> Artwork(
            url = "${baseUrl}img/playlist/$customImage",
            shape = ArtworkShape
        )

        playlist.images.isEmpty() -> Artwork(url = "", shape = ArtworkShape)

        else -> {
            val cell = (HomeCardWidth - 1.dp) / 2
            val tiles = List(4) { playlist.images[it % playlist.images.size] }
            Column(
                modifier = Modifier
                    .size(HomeCardWidth)
                    .clip(ArtworkShape),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                tiles.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        row.forEach { image ->
                            Artwork(
                                url = "${baseUrl}img/thumbnail/small/$image",
                                shape = RoundedCornerShape(0.dp),
                                size = cell
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MixArtwork(mix: Mix, baseUrl: String) {
    // The server draws its own label and fade onto mix covers, so show those as they are
    val cover = mix.image
    if (cover != null) {
        Artwork(url = "${baseUrl}img/mix/medium/$cover", shape = ArtworkShape)
        return
    }

    val tint = parseServerColor(mix.color)
    val isLight = tint != null &&
            (0.2126F * tint.red + 0.7152F * tint.green + 0.0722F * tint.blue) > 0.5F
    val side = HomeCardWidth / 2

    Box(
        modifier = Modifier
            .size(HomeCardWidth)
            .clip(ArtworkShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        // Later images overlap earlier ones, each a quarter of the width further right
        mix.images.take(3).forEachIndexed { index, image ->
            val folder = if (image.isArtist) "artist/medium" else "thumbnail/medium"
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${baseUrl}img/$folder/${image.image}")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(x = HomeCardWidth * 0.25F * index, y = (HomeCardWidth - side) / 2)
                    .size(side)
                    .shadow(elevation = 2.dp)
            )
        }

        if (tint != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val under = cssLinearGradient(
                            degrees = -17F,
                            size = size,
                            stops = arrayOf(0.10F to tint, 0.30F to tint.copy(alpha = 0F))
                        )
                        val over = cssLinearGradient(
                            degrees = 27F,
                            size = size,
                            stops = arrayOf(0.21F to tint, 1F to tint.copy(alpha = 0.15F))
                        )
                        onDrawBehind {
                            drawRect(under)
                            drawRect(over)
                        }
                    }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(HomeCardWidth * 0.05F)
        ) {
            Text(
                text = "${mix.type.replaceFirstChar { it.uppercase() }} Mix",
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    tint == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    isLight -> Color(0xFF6D4510)
                    else -> Color(0xFFAC8E68)
                }
            )
            Text(
                text = mix.title.replace("Radio", "").trim(),
                fontSize = 13.5.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                color = tint?.shiftTowards(if (isLight) Color.Black else Color.White, 0.8F)
                    ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** A CSS `linear-gradient(<degrees>deg, ...)` spanning [size], as the web and iOS draw mix covers. */
private fun cssLinearGradient(
    degrees: Float,
    size: Size,
    stops: Array<Pair<Float, Color>>
): Brush {
    val radians = Math.toRadians(degrees.toDouble())
    val sin = sin(radians).toFloat()
    val cos = cos(radians).toFloat()
    val length = abs(size.width * sin) + abs(size.height * cos)
    val dx = sin * length / 2
    val dy = -cos * length / 2
    val center = size.center
    return Brush.linearGradient(
        colorStops = stops,
        start = Offset(center.x - dx, center.y - dy),
        end = Offset(center.x + dx, center.y + dy)
    )
}

private fun Color.shiftTowards(target: Color, amount: Float): Color = Color(
    red = red + (target.red - red) * amount,
    green = green + (target.green - green) * amount,
    blue = blue + (target.blue - blue) * amount
)

/** Parses a server colour, sent as "rgb(r, g, b)" or "#rrggbb". */
private fun parseServerColor(value: String?): Color? {
    val text = value?.trim() ?: return null
    if (text.startsWith("#") && text.length == 7) {
        return text.drop(1).toLongOrNull(16)?.let { Color(0xFF000000 or it) }
    }
    if (!text.startsWith("rgb", ignoreCase = true)) return null
    val parts = Regex("[0-9.]+").findAll(text).map { it.value.toFloat() }.toList()
    if (parts.size < 3) return null
    return Color(red = parts[0] / 255F, green = parts[1] / 255F, blue = parts[2] / 255F)
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, widthDp = 800)
@Composable
private fun HomeCardTypesPreview() {
    val artist = Artist(
        artistHash = "juice",
        colors = emptyList(),
        createdDate = 0.0,
        helpText = "",
        image = "",
        name = "Juice WRLD"
    )
    val track = Track(
        album = "Certified Lover Boy",
        albumTrackArtists = emptyList(),
        albumHash = "clb",
        trackArtists = listOf(TrackArtist(artistHash = "drake", image = "", name = "Drake")),
        bitrate = 320,
        duration = 248,
        filepath = "",
        folder = "",
        image = "",
        isFavorite = false,
        title = "Toosie Slide",
        trackHash = "toosie",
        disc = 1,
        trackNumber = 1
    )
    val items = listOf(
        HomeItem.ArtistItem(artist),
        HomeItem.AlbumItem(
            Album(
                albumArtists = listOf(artist),
                albumHash = "lnd",
                colors = emptyList(),
                createdDate = 0.0,
                date = 2020,
                helpText = "",
                image = "",
                title = "Legends Never Die",
                versions = emptyList()
            )
        ),
        HomeItem.TrackItem(track),
        HomeItem.PlaylistItem(
            Playlist(
                id = "7",
                name = "Late Night Drive",
                customImage = null,
                images = listOf("a", "b", "c", "d"),
                trackCount = 32
            )
        ),
        HomeItem.MixItem(
            Mix(
                id = "1",
                title = "Juice WRLD Radio",
                description = "",
                sourceHash = "juice",
                ogSourceHash = "juice",
                type = "artist",
                image = null,
                color = "rgb(168, 116, 58)",
                images = listOf(MixImage("a", true), MixImage("b", false), MixImage("c", false)),
                trackCount = 40
            )
        )
    )

    SwingMusicTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeCard(
                item = HomeItem.TrackItem(track),
                baseUrl = "",
                isLoading = false,
                onClick = {},
                nowPlayingState = PlaybackState.PLAYING
            )
            items.forEach { item ->
                HomeCard(
                    item = item,
                    baseUrl = "",
                    isLoading = item is HomeItem.PlaylistItem,
                    onClick = {}
                )
            }
        }
    }
}

