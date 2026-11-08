package com.android.swingmusic.uicomponent.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.android.swingmusic.core.domain.model.Listening
import com.android.swingmusic.core.domain.model.ListeningRank
import com.android.swingmusic.core.domain.model.ListeningTopAlbum
import com.android.swingmusic.core.domain.model.ListeningTopTrack
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.formattedAlbumDuration

/**
 * The current user's all-time listening for an artist (or, later, an album): rank, time
 * listened, plays, top album and top track. Shown only when there is something to show.
 */
@Composable
fun ListeningStatsSection(
    listening: Listening,
    baseUrl: String,
    onAlbumClick: (albumHash: String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Your listening",
) {
    Column(modifier = modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listening.rank?.let { rank ->
                StatCard(
                    modifier = Modifier.weight(1F),
                    value = "#${rank.position}",
                    label = "of ${rank.total} ${rank.of}"
                )
            }
            StatCard(
                modifier = Modifier.weight(1F),
                value = listening.playDuration.formattedAlbumDuration(),
                label = "listened all time"
            )
            StatCard(
                modifier = Modifier.weight(1F),
                value = if (listening.playCount == 1) "1 play" else "${listening.playCount} plays",
                label = listening.lastPlayedAgo?.let { "last played $it" } ?: "not played yet"
            )
        }
        if (listening.topAlbum != null || listening.topTrack != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listening.topAlbum?.let { album ->
                    TopItemCard(
                        modifier = Modifier
                            .weight(1F)
                            .clickable { onAlbumClick(album.albumHash) },
                        imageUrl = "${baseUrl}img/thumbnail/small/${album.image}",
                        title = album.title,
                        label = "top album · ${album.playDuration.formattedAlbumDuration()}"
                    )
                }
                listening.topTrack?.let { track ->
                    TopItemCard(
                        modifier = Modifier.weight(1F),
                        imageUrl = "${baseUrl}img/thumbnail/small/${track.image}",
                        title = track.title,
                        label = "top track · ${track.playDuration.formattedAlbumDuration()}"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(14.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        BasicText(
            text = value,
            style = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 13.sp, maxFontSize = 22.sp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TopItemCard(imageUrl: String, title: String, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.audio_fallback),
            fallback = painterResource(R.drawable.audio_fallback),
            error = painterResource(R.drawable.audio_fallback),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun ListeningStatsSectionPreview() {
    SwingMusicTheme {
        ListeningStatsSection(
            listening = Listening(
                playDuration = 100_920,
                playCount = 321,
                lastPlayedAgo = "3 days ago",
                rank = ListeningRank(position = 3, total = 120, of = "artists"),
                topAlbum = ListeningTopAlbum("a", "Take Care", "a.webp", "Drake", 36_000),
                topTrack = ListeningTopTrack("t", "Marvins Room", "a.webp", 7_200)
            ),
            baseUrl = "",
            onAlbumClick = {}
        )
    }
}
