package com.android.swingmusic.uicomponent.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.android.swingmusic.uicomponent.presentation.util.Screen

@Composable
fun AlbumItem(
    modifier: Modifier,
    screen: Screen = Screen.ALL_ALBUMS,
    album: Album,
    baseUrl: String,
    onClick: (albumHash: String) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val versionContainerColor = if (isDarkTheme) Color(0x26DACC32) else Color(0x3D744F00)
    val versionTextColor = if (isDarkTheme) Color(0xFFDACC32) else Color(0xFF744E00)

    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick(album.albumHash)
            }
            .padding(
                top = 16.dp,
                bottom = 8.dp,
                start = 8.dp,
                end = 8.dp
            )
    ) {
        AsyncImage(
            modifier = modifier
                .clip(RoundedCornerShape(10))
                .clickable { onClick(album.albumHash) }
                .border(
                    width = (.1).dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1F),
                    shape = RoundedCornerShape(10)
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data("${baseUrl}img/thumbnail/medium/${album.image}")
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.audio_fallback),
            fallback = painterResource(R.drawable.audio_fallback),
            error = painterResource(R.drawable.audio_fallback),
            contentDescription = "Artist Image",
            contentScale = ContentScale.FillWidth,
        )

        if (album.helpText.isNotEmpty()
            && (screen ==
                    Screen.ALL_ALBUMS ||
                    screen == Screen.HOME ||
                    screen == Screen.SEARCH)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = album.helpText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = album.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )

        if (screen != Screen.ARTIST) {
            if (album.albumArtists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = album.albumArtists[0].name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = album.helpText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
            )
        }

        if (album.versions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                items(album.versions) { version ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(14))
                            .background(versionContainerColor)
                            .padding(horizontal = 5.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = version.uppercase(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = versionTextColor
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AlbumItemPreview() {
    val albumArtists = Artist(
        "",
        emptyList(),
        0.0,
        "",
        "",
        "Juice Wrld"
    )
    val album = Album(
        albumArtists = listOf(albumArtists),
        albumHash = "hash",
        colors = emptyList(),
        createdDate = 0.0,
        date = 0,
        helpText = "12 Tracks",
        image = "",
        title = "Death Race For Love",
        versions = listOf("bonus edition")
    )
    SwingMusicTheme_Preview {
        Surface {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                columns = GridCells.Fixed(2),
                state = rememberLazyGridState(),
            ) {
                items(3) {
                    AlbumItem(
                        modifier = Modifier.fillMaxWidth(),
                        screen = Screen.ARTIST,
                        album = album,
                        baseUrl = "",
                        onClick = {}
                    )
                }
                item {
                    AlbumItem(
                        modifier = Modifier.fillMaxWidth(),
                        screen = Screen.SEARCH,
                        album = album.copy(versions = emptyList()),
                        baseUrl = "",
                        onClick = {}
                    )
                }
            }

        }
    }
}
