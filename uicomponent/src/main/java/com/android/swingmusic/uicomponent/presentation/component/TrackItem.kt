package com.android.swingmusic.uicomponent.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.formatDuration

@Composable
fun TrackItem(
    track: Track,
    isAlbumTrack: Boolean = false,
    isCurrentTrack: Boolean = false,
    showMenuIcon: Boolean = false,
    playbackState: PlaybackState = PlaybackState.PAUSED,
    onClickTrackItem: () -> Unit,
    onClickMoreVert: (Track) -> Unit,
    baseUrl: String
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .clip(RoundedCornerShape(20)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Image, Title, Artists, Duration
        Row(
            modifier = Modifier
                .background(
                    color = if (isCurrentTrack)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = .14F) else
                        Color.Unspecified
                )
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = { onClickTrackItem() }
                )
                .padding(
                    start = if(isAlbumTrack) 2.dp else 8.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track number for album tracks (positioned before image)
                if (isAlbumTrack) {
                    Box(
                        modifier = Modifier
                            .width(40.dp) // Fixed width for alignment
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackNumberStr = track.trackNumber.toString()
                        if(track.trackNumber > 0) {
                            if (trackNumberStr.length <= 3) {
                                // Single line for 1-3 digit numbers (1-999)
                                Text(
                                    text = trackNumberStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End
                                )
                            } else {
                                // Split into two lines for 4+ digit numbers (1000+)
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = trackNumberStr.take(3),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = trackNumberStr.drop(3),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }

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
                        modifier = Modifier.fillMaxSize(),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("${baseUrl}img/thumbnail/small/${track.image}")
                            .build(),
                        placeholder = painterResource(R.drawable.audio_fallback),
                        fallback = painterResource(R.drawable.audio_fallback),
                        error = painterResource(R.drawable.audio_fallback),
                        contentDescription = "Track Image",
                        contentScale = ContentScale.Crop
                    )

                    if (isCurrentTrack) {
                        PlayingTrackIndicator(playbackState = playbackState)
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = track.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val artistsJoined = track.trackArtists.joinToString(", ") { it.name }
                        Text(
                            text = artistsJoined,
                            modifier = Modifier.weight(1f, fill = false),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Always reserve space for menu icon to maintain consistent alignment
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showMenuIcon) {
                    IconButton(onClick = { onClickMoreVert(track) }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Icon"
                        )
                    }
                }
            }
        }

        /*trackQueueNumber?.let { number ->
            if (!isCurrentTrack)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .wrapContentSize()
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
        }*/
    }
}


@Preview(
    showBackground = true,
    device = Devices.PIXEL_6,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun TrackItemPreview() {

    // Edge case artists
    val shortArtist = TrackArtist(
        artistHash = "short123",
        image = "short.jpg",
        name = "Ed"
    )
    val longArtist = TrackArtist(
        artistHash = "long123",
        image = "long.jpg",
        name = "Artist With Long Name That Should Overflow"
    )
    val multipleArtists = listOf(
        TrackArtist("artist1", "img1.jpg", "The Beatles"),
        TrackArtist("artist2", "img2.jpg", "Paul McCartney"),
        TrackArtist("artist3", "img3.jpg", "John Lennon"),
        TrackArtist("artist4", "img4.jpg", "George Harrison"),
        TrackArtist("artist5", "img5.jpg", "Ringo Starr")
    )

    // Edge case tracks
    val shortTrack = Track(
        album = "Short", albumTrackArtists = listOf(shortArtist), albumHash = "short123",
        trackArtists = listOf(shortArtist), bitrate = 320, duration = 30,
        filepath = "/short.mp3", folder = "/short", image = "short.jpg",
        isFavorite = false, title = "Hi", trackHash = "short123", disc = 1, trackNumber = 1
    )

    val longTrack = Track(
        album = "Very Long Album Name That Should Overflow",
        albumTrackArtists = listOf(longArtist),
        albumHash = "long123",
        trackArtists = listOf(longArtist),
        bitrate = 320,
        duration = 3661, // 1 hour 1 minute 1 second
        filepath = "/very/long/path/to/track.mp3",
        folder = "/very/long/path",
        image = "long.jpg",
        isFavorite = true,
        title = "This Is An Extremely Long Track Title That Should Overflow",
        trackHash = "long123",
        disc = 1,
        trackNumber = 1
    )

    val multipleArtistsTrack = Track(
        album = "Collaborative Album",
        albumTrackArtists = multipleArtists,
        albumHash = "multi123",
        trackArtists = multipleArtists,
        bitrate = 320,
        duration = 240,
        filepath = "/collab.mp3",
        folder = "/collab",
        image = "collab.jpg",
        isFavorite = true,
        title = "Song with Many Artists",
        trackHash = "multi123",
        disc = 1,
        trackNumber = 1
    )

    SwingMusicTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                Text(
                    text = "Edge Cases Preview",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                // Folders first (as they appear in the app)
                FolderItem(
                    folder = Folder(
                        trackCount = 24,
                        folderCount = 3,
                        isSym = false,
                        name = "Rock Albums",
                        path = "/music/rock"
                    ),
                    onClickFolderItem = { },
                    onClickMoreVert = { }
                )

                FolderItem(
                    folder = Folder(
                        trackCount = 0,
                        folderCount = 8,
                        isSym = false,
                        name = "Very Long Folder Name That Should Ellipse Properly",
                        path = "/music/long"
                    ),
                    onClickFolderItem = { },
                    onClickMoreVert = { }
                )

                // Then tracks
                TrackItem(
                    isCurrentTrack = false,
                    playbackState = PlaybackState.PAUSED,
                    showMenuIcon = true,
                    track = shortTrack,
                    onClickTrackItem = { },
                    onClickMoreVert = { },
                    baseUrl = ""
                )

                TrackItem(
                    isCurrentTrack = true,
                    playbackState = PlaybackState.PLAYING,
                    showMenuIcon = true,
                    track = multipleArtistsTrack,
                    onClickTrackItem = { },
                    onClickMoreVert = { },
                    baseUrl = ""
                )

                TrackItem(
                    isCurrentTrack = false,
                    playbackState = PlaybackState.PAUSED,
                    showMenuIcon = true,
                    track = longTrack,
                    onClickTrackItem = { },
                    onClickMoreVert = { },
                    baseUrl = ""
                )

                TrackItem(
                    isCurrentTrack = false,
                    playbackState = PlaybackState.PAUSED,
                    showMenuIcon = false,
                    track = longTrack.copy(title = "No Menu Track For Alignment Test"),
                    onClickTrackItem = { },
                    onClickMoreVert = { },
                    baseUrl = ""
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_6,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
@Composable
fun AlbumTrackItemPreview() {

    val albumArtist = TrackArtist(
        artistHash = "album123",
        image = "album.jpg",
        name = "Album Artist"
    )

    val featuredArtist = TrackArtist(
        artistHash = "featured123",
        image = "featured.jpg",
        name = "Featured Artist With A Long Name"
    )

    // Album tracks with different scenarios including extreme track numbers
    val albumTracks = listOf(
        Track(
            album = "Sample Album",
            albumTrackArtists = listOf(albumArtist),
            albumHash = "album123",
            trackArtists = listOf(albumArtist),
            bitrate = 320,
            duration = 180,
            filepath = "/album/track1.mp3",
            folder = "/album",
            image = "album.jpg",
            isFavorite = false,
            title = "Opening Track",
            trackHash = "track1",
            disc = 1,
            trackNumber = 1
        ),
        Track(
            album = "Sample Album",
            albumTrackArtists = listOf(albumArtist),
            albumHash = "album123",
            trackArtists = listOf(albumArtist),
            bitrate = 320,
            duration = 240,
            filepath = "/album/track2.mp3",
            folder = "/album",
            image = "album.jpg",
            isFavorite = true,
            title = "Currently Playing Track",
            trackHash = "track2",
            disc = 1,
            trackNumber = 12
        ),
        Track(
            album = "Sample Album",
            albumTrackArtists = listOf(albumArtist),
            albumHash = "album123",
            trackArtists = listOf(albumArtist, featuredArtist),
            bitrate = 320,
            duration = 320,
            filepath = "/album/track3.mp3",
            folder = "/album",
            image = "album.jpg",
            isFavorite = false,
            title = "Double Digit Track",
            trackHash = "track3",
            disc = 1,
            trackNumber = 99
        ),
        Track(
            album = "Sample Album",
            albumTrackArtists = listOf(albumArtist),
            albumHash = "album123",
            trackArtists = listOf(albumArtist),
            bitrate = 320,
            duration = 420,
            filepath = "/album/track4.mp3",
            folder = "/album",
            image = "album.jpg",
            isFavorite = true,
            title = "Triple Digit Track Number",
            trackHash = "track4",
            disc = 1,
            trackNumber = 123
        ),
        Track(
            album = "Sample Album",
            albumTrackArtists = listOf(albumArtist),
            albumHash = "album123",
            trackArtists = listOf(albumArtist),
            bitrate = 320,
            duration = 12,
            filepath = "/album/track5.mp3",
            folder = "/album",
            image = "album.jpg",
            isFavorite = false,
            title = "Extreme Track Number",
            trackHash = "track5",
            disc = 1,
            trackNumber = 9999
        ),
        Track(
            album = "Sample Album",
            albumTrackArtists = listOf(albumArtist),
            albumHash = "album123",
            trackArtists = listOf(albumArtist),
            bitrate = 583,
            duration = 12,
            filepath = "/album/track5.mp3",
            folder = "/album",
            image = "album.jpg",
            isFavorite = false,
            title = "Zero Track Number",
            trackHash = "track5",
            disc = 1,
            trackNumber = 0
        )
    )

    SwingMusicTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                Text(
                    text = "Album Track Items",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                albumTracks.forEachIndexed { index, track ->
                    TrackItem(
                        track = track,
                        isAlbumTrack = true,
                        isCurrentTrack = index == 1, // Second track is playing
                        showMenuIcon = true,
                        playbackState = if (index == 1) PlaybackState.PLAYING else PlaybackState.PAUSED,
                        onClickTrackItem = { },
                        onClickMoreVert = { },
                        baseUrl = ""
                    )
                }
            }
        }
    }
}
