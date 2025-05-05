package com.android.swingmusic.uicomponent.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.TopResultItem
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.android.swingmusic.uicomponent.presentation.util.BlurTransformation

@Composable
fun TopSearchResultItem(
    baseUrl: String,
    isLoadingTracks: Boolean = false,
    topResultItem: TopResultItem,
    onClickTopResultItem: (type: String, hash: String) -> Unit,
    onClickPlayTopResultItem: (type: String, hash: String) -> Unit
) {
    val imagePath = if (topResultItem.type == "artist") {
        "${baseUrl}img/artist/${topResultItem.image}"
    } else {
        "${baseUrl}img/thumbnail/${topResultItem.image}"
    }

    val backgroundImagePath = if (topResultItem.type == "artist") {
        "${baseUrl}img/artist/small/${topResultItem.image}"
    } else {
        "${baseUrl}img/thumbnail/small/${topResultItem.image}"
    }

    val displayTitle = if (topResultItem.type == "artist")
        topResultItem.name else topResultItem.title

    val displayType = topResultItem.type.replaceFirstChar { it.uppercaseChar() }

    val hash = when (topResultItem.type) {
        "artist" -> topResultItem.artistHash
        "track" -> topResultItem.trackHash
        "album" -> topResultItem.albumHash
        else -> "hash"
    }

    Card(shape = RoundedCornerShape(11.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clickable {
                    onClickTopResultItem(topResultItem.type, hash)
                },
            contentAlignment = Alignment.CenterStart
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp)),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backgroundImagePath)
                    .transformations(
                        listOf(
                            BlurTransformation(
                                scale = 0.5f,
                                radius = 25
                            )
                        )
                    ).build(),
                contentDescription = "Image",
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 1f)
                            )
                        )
                    )
            )

            AsyncImage(
                modifier = Modifier
                    .size(170.dp)
                    .padding(8.dp)
                    .clip(
                        if (topResultItem.type == "artist") CircleShape else
                            RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        onClickTopResultItem(topResultItem.type, hash)
                    },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePath)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.artist_fallback),
                fallback = painterResource(R.drawable.artist_fallback),
                error = painterResource(R.drawable.artist_fallback),
                contentDescription = "Image",
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxHeight()
                    .padding(
                        top = 24.dp,
                        bottom = 24.dp,
                        start = 174.dp, // image size of 170.dp considered
                        end = 4.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32))
                            .background(MaterialTheme.colorScheme.primary),
                        onClick = {
                            onClickPlayTopResultItem(topResultItem.type, hash)
                        }
                    ) {
                        if (isLoadingTracks) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.scale(0.75F),
                                strokeWidth = (0.84).dp
                            )
                        } else
                            Icon(
                                painter = painterResource(id = R.drawable.play_arrow_fill),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = "Play Icon"
                            )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .25F))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayType,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = displayTitle.ifBlank { "Unknown" },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )

                if (displayType == "Album" || displayType == "Track") {
                    val artists = if (displayType == "Album") {
                        topResultItem.albumArtists.joinToString(", ") { it.name }
                    } else {
                        topResultItem.artists.joinToString(", ") { it.name }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = artists,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84F),
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
private fun TopSearchResultItemPreview() {
    SwingMusicTheme_Preview {
        TopSearchResultItem(
            baseUrl = "",
            isLoadingTracks = false,
            topResultItem = topResult,
            onClickTopResultItem = { _, _ -> },
            onClickPlayTopResultItem = { _, _ -> }
        )
    }
}

private val topResult = TopResultItem(
    type = "artist",
    albumCount = 0,
    artistHash = "hash",
    albumHash = "436f1e93ac4087d1",
    trackHash = "7e3bc5770fd9a362",
    genres = emptyList(),
    trackCount = 0,
    albumcount = 0,
    color = "",
    createdDate = 0,
    date = 0,
    duration = 166,
    favUserIds = emptyList(),
    genreHashes = "",
    id = 0,
    image = "436f1e93ac4087d1.webp",
    lastPlayed = 0,
    name = "Preview",
    playCount = 0,
    playDuration = 0,
    trackcount = 0,
    album = "F U Goodbye",
    albumArtists = listOf(),
    artistHashes = listOf("7684631669f8"),
    artists = listOf(),
    bitrate = 320,
    explicit = false,
    filepath = "/home/Music/Chill/Claire Radio/Peach PRC - F U Goodbye.m4a",
    folder = "/home/Music/Chill/Claire Radio/",
    isFavorite = false,
    title = "F U Goodbye"
)
