package com.android.swingmusic.uicomponent.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.util.PlayerState
import com.android.swingmusic.network.data.util.BASE_URL
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.formatDuration
import com.android.swingmusic.uicomponent.presentation.util.trimString


@Composable
fun TrackItem(
    track: Track,
    isCurrentTrack: Boolean = false,
    playerState: PlayerState = PlayerState.UNSPECIFIED,
    onClickTrackItem: (Track) -> Unit,
    onClickMoreVert: (Track) -> Unit
) {
    SwingMusicTheme {
        Surface(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 12.dp)
                .clip(RoundedCornerShape(20))
        ) {
            // Image, Title, Artists, Duration
            Row(
                modifier = Modifier
                    .background(
                        color = if (isCurrentTrack)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = .14F) else
                            Color.Unspecified
                    )
                    .clickable {
                        onClickTrackItem(track)
                    }
                    .padding(all = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(16))
                            .size(48.dp)
                            .border(
                                width = (.1).dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1F),
                                shape = RoundedCornerShape(16)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("${BASE_URL}img/t/s/${track.image}")
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.audio_fallback),
                            fallback = painterResource(R.drawable.audio_fallback),
                            error = painterResource(R.drawable.audio_fallback),
                            contentDescription = "Track Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isCurrentTrack) {
                            PlayingTrackIndicator(playerState = playerState)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .scrollable(
                                orientation = Orientation.Horizontal,
                                state = rememberScrollState()
                            )
                    ) {
                        Text(
                            text = track.title.trimString(32),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var artists = ""

                            for ((index, artist) in track.artists.withIndex()) {
                                artists += artist.name

                                if (track.artists.lastIndex != index) {
                                    artists += ", "
                                }
                            }

                            Text(
                                text = artists.trimString(36),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Dot Separator
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(CircleShape)
                                    .size(3.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .50F)
                                    )
                            )

                            Text(
                                text = track.duration.formatDuration(),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .80F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // More Icon
                IconButton(onClick = { onClickMoreVert(track) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Icon"
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_2,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun TrackItemPreview() {

    val lilPeep = Artist(
        artistHash = "lilpeep123",
        image = "lilpeep.jpg",
        name = "Lil Peep"
    )
    val juice = Artist(
        artistHash = "juice123",
        image = "juice.jpg",
        name = "Juice WRLD"
    )

    val albumArtists = listOf(lilPeep, juice)
    val artists = listOf(lilPeep, juice)
    val genre = listOf("Rap", "Emo")

    val track = Track(
        album = "Sample Album",
        albumArtists = albumArtists,
        albumHash = "albumHash123",
        artistHashes = "artistHashes123",
        artists = artists,
        ati = "ati123",
        bitrate = 320,
        copyright = "Copyright © 2024",
        createdDate = 1648731600.0, // Sample timestamp
        date = 2024,
        disc = 1,
        duration = 454, // Sample duration in seconds
        filepath = "/path/to/track.mp3",
        folder = "/path/to/album",
        genre = genre,
        image = "/path/to/album/artwork.jpg",
        isFavorite = true,
        lastMod = 1648731600, // Sample timestamp
        ogAlbum = "Original Album",
        ogTitle = "Original Title",
        pos = 1,
        title = "Sample Track",
        track = 1,
        trackHash = "trackHash123"
    )

    SwingMusicTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                val demoFolder =
                    Folder((0..6).random(), (0..6).random(), false, "Sample Folder", "/home")
                FolderItem(
                    folder = demoFolder,
                    onClickFolderItem = {

                    },
                    onClickMoreVert = {

                    }
                )

                FolderItem(
                    folder = demoFolder,
                    onClickFolderItem = {

                    },
                    onClickMoreVert = {

                    }
                )
                TrackItem(
                    isCurrentTrack = false,
                    playerState = PlayerState.PAUSED,
                    track = track,
                    onClickTrackItem = {

                    },
                    onClickMoreVert = {

                    }
                )
                TrackItem(
                    isCurrentTrack = false,
                    track = track,
                    onClickTrackItem = {

                    },
                    onClickMoreVert = {

                    }
                )
                TrackItem(
                    isCurrentTrack = false,
                    track = track,
                    onClickTrackItem = {

                    },
                    onClickMoreVert = {

                    }
                )
                TrackItem(
                    isCurrentTrack = true,
                    playerState = PlayerState.PAUSED,
                    track = track,
                    onClickTrackItem = {

                    },
                    onClickMoreVert = {

                    }
                )
            }
        }
    }
}
