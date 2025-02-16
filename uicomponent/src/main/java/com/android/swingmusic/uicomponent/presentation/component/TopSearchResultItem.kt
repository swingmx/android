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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.android.swingmusic.core.domain.model.TopResult
import com.android.swingmusic.core.domain.model.TopResultItem
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.android.swingmusic.uicomponent.presentation.util.BlurTransformation

@Composable
fun TopSearchResultItem(
    baseUrl: String,
    topResult: TopResult,
    onClickTopResultItem: (type: String, hash: String) -> Unit
) {
    val imagePath = if (topResult.type == "artist") {
        "${baseUrl}img/artist/${topResult.item?.image}"
    } else {
        "${baseUrl}img/thumbnail/${topResult.item?.image}"
    }

    val displayTitle = if (topResult.type == "artist")
        topResult.item?.name else topResult.item?.title

    val type = topResult.type.replaceFirstChar { it.uppercaseChar() }
    val hash = when (topResult.type) {
        "artist" -> topResult.item?.artistHash
        "track" -> topResult.item?.trackHash
        "album" -> topResult.item?.albumHash
        else -> "hash"
    }

    Surface(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePath)
                    .crossfade(true)
                    .transformations(
                        listOf(
                            BlurTransformation(
                                scale = 0.5f,
                                radius = 25
                            )
                        )
                    )
                    .build(),
                placeholder = painterResource(R.drawable.artist_fallback),
                fallback = painterResource(R.drawable.artist_fallback),
                error = painterResource(R.drawable.artist_fallback),
                contentDescription = "Image",
                contentScale = ContentScale.Crop,
            )

            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(0.47f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .fillMaxHeight()
                    .clickable {
                        onClickTopResultItem(topResult.type, hash ?: "hash")
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

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(.5F)
                    .fillMaxHeight()
                    .padding(vertical = 24.dp),
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
                            onClickTopResultItem(topResult.type, hash ?: "hash")
                        }
                    ) {
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
                        text = type,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (displayTitle.isNullOrBlank()) "Unknown" else displayTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TopSearchResultItemPreview() {
    SwingMusicTheme_Preview {
        TopSearchResultItem(
            baseUrl = "",
            topResult = topResult,
            onClickTopResultItem = { _, _ -> }
        )
    }
}

private val topResult = TopResult(
    item = TopResultItem(
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
        name = "",
        playCount = 0,
        playDuration = 0,
        trackcount = 0,
        album = "F U Goodbye",
        albumArtists = listOf(),
        artistHashes = listOf("76846ddfb31669f8"),
        artists = listOf(),
        bitrate = 320,
        explicit = false,
        filepath = "/home/cwilvx/Music/Chill/Claire Rosinkranz Radio/Peach PRC - F U Goodbye.m4a",
        folder = "/home/cwilvx/Music/Chill/Claire Rosinkranz Radio/",
        isFavorite = false,
        title = "F U Goodbye"
    ),
    type = "track"
)
