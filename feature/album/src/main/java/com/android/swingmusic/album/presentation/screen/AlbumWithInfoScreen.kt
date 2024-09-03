package com.android.swingmusic.album.presentation.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.album.presentation.event.AlbumWithInfoUiEvent
import com.android.swingmusic.album.presentation.navigator.AlbumNavigator
import com.android.swingmusic.album.presentation.viewmodel.AlbumWithInfoViewModel
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AlbumInfo
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Genre
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.ramcosta.composedestinations.annotation.Destination
import java.time.Instant
import java.time.ZoneOffset

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AlbumWithInfo(
    currentTrack: Track?,
    playbackState: PlaybackState,
    albumInfo: AlbumInfo,
    albumTracks: List<Track>,
    baseUrl: String,
    onClickBack: () -> Unit,
    onClickMore: () -> Unit,
    onClickArtist: (artistHsh: String) -> Unit,
    onClickAlbumTrack: (index: Int, queue: List<Track>) -> Unit,
    onPlay: (queue: List<Track>) -> Unit,
    onShuffle: () -> Unit,
    onToggleFavorite: (Boolean, String) -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val versionContainerColor = if (isDarkTheme) Color(0x26DACC32) else Color(0x3D744F00)
    val versionTextColor = if (isDarkTheme) Color(0xFFDACC32) else Color(0xFF744E00)

    val interaction = remember { MutableInteractionSource() }
    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .padding()
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            state = listState,
        ) {
            item {
                Box {
                    AsyncImage(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .fillParentMaxHeight(.5F),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("${baseUrl}img/thumbnail/${albumInfo.image}")
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.audio_fallback),
                        fallback = painterResource(R.drawable.audio_fallback),
                        error = painterResource(R.drawable.audio_fallback),
                        contentDescription = "Artist Image",
                        contentScale = ContentScale.Crop,
                    )

                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .fillParentMaxHeight(.5F)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = .45F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .5F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .6F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .7F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .8F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .9F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .95F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 1F)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillParentMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(top = 24.dp)
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                modifier = Modifier
                                    .clip(CircleShape),
                                // .background(iconColor),
                                onClick = {
                                    onClickBack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back Arrow"
                                )
                            }

                            IconButton(
                                modifier = Modifier
                                    .clip(CircleShape),
                                // .background(iconColor),
                                onClick = {
                                    onClickMore()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Back Arrow"
                                )
                            }
                        }

                        AsyncImage(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth(.5F)
                                .fillParentMaxHeight(.25F)
                                .clip(RoundedCornerShape(12))
                                .shadow(elevation = 12.dp)
                                .border(
                                    width = (.5).dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1F),
                                    shape = RoundedCornerShape(12)
                                ),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("${baseUrl}img/thumbnail/medium/${albumInfo.image}")
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.audio_fallback),
                            fallback = painterResource(R.drawable.audio_fallback),
                            error = painterResource(R.drawable.audio_fallback),
                            contentDescription = "Artist Image",
                            contentScale = ContentScale.Crop,
                        )

                        Text(
                            modifier = Modifier.padding(
                                start = 12.dp,
                                end = 12.dp,
                                top = 16.dp,
                                bottom = 2.dp
                            ),
                            text = albumInfo.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        LazyRow(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            item {
                                Text(
                                    text = "Album by ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            itemsIndexed(albumInfo.albumArtists) { index, artist ->
                                Text(
                                    modifier = Modifier.clickable(
                                        interactionSource = interaction,
                                        indication = null
                                    ) {
                                        onClickArtist(artist.artisthash)
                                    },
                                    text = artist.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (index != albumInfo.albumArtists.lastIndex) {
                                    Text(text = ",")
                                }

                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            item {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F),
                                    contentDescription = "Arrow forward"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            item {
                                Text(
                                    text = albumInfo.date.toYear(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            items(albumInfo.versions) { version ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(14))
                                        .background(versionContainerColor)
                                        .padding(horizontal = 5.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$version".uppercase(),
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

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                Button(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = MaterialTheme.colorScheme.surface,
                                        containerColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    onClick = {
                                        onPlay(albumTracks)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.play_arrow),
                                        contentDescription = "Play Icon"
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Play",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            item {
                                OutlinedButton(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    onClick = {
                                        onShuffle()
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.shuffle),
                                        contentDescription = "Play Icon"
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Shuffle",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            item {
                                IconButton(
                                    modifier = Modifier
                                        .clip(CircleShape),
                                    onClick = {
                                        onToggleFavorite(albumInfo.isFavorite, albumInfo.albumHash)
                                    }
                                ) {
                                    val icon =
                                        if (albumInfo.isFavorite) R.drawable.fav_filled
                                        else R.drawable.fav_not_filled
                                    Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = "Favorite"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(
                items = albumTracks,
                key = { _: Int, item: Track -> item.filepath }
            ) { index, track ->
                TrackItem(
                    track = track,
                    isCurrentTrack = track.trackHash == currentTrack?.trackHash,
                    playbackState = playbackState,
                    onClickTrackItem = {
                        onClickAlbumTrack(
                            index, albumTracks
                        )
                    },
                    onClickMoreVert = {
                        // Show menu for Album Track
                    },
                    baseUrl = baseUrl
                )
                if (index == albumTracks.lastIndex) {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Destination
@Composable
fun AlbumWithInfoScreen(
    albumWithInfoViewModel: AlbumWithInfoViewModel = hiltViewModel(),
    mediaControllerViewModel: MediaControllerViewModel,
    albumNavigator: AlbumNavigator,
    albumHash: String,
) {
    val albumWithInfoState by albumWithInfoViewModel.albumWithInfoState

    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    SideEffect {
        albumWithInfoViewModel.onAlbumWithInfoUiEvent(
            AlbumWithInfoUiEvent.OnLoadAlbumWithInfo(albumHash)
        )
    }

    SwingMusicTheme {
        when (albumWithInfoState.albumWithInfo) {
            is Resource.Loading -> {
                // TODO: Use Shimmer Loader
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = albumWithInfoState.albumWithInfo.message
                            ?: "Failed to load Album"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            albumWithInfoViewModel.onAlbumWithInfoUiEvent(
                                event = AlbumWithInfoUiEvent.OnRefreshAlbumInfo
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(text = "RETRY")
                    }
                }
            }

            is Resource.Success -> {
                AlbumWithInfo(
                    currentTrack = playerUiState.nowPlayingTrack,
                    playbackState = playerUiState.playbackState,
                    albumInfo = albumWithInfoState.albumWithInfo.data!!.albumInfo,
                    albumTracks = albumWithInfoState.albumWithInfo.data!!.tracks,
                    baseUrl = baseUrl ?: "https://default",
                    onClickBack = { albumNavigator.navigateBack() },
                    onClickMore = {

                    },
                    onClickArtist = { artistHash ->

                    },
                    onPlay = { queue ->
                        mediaControllerViewModel.onQueueEvent(
                            QueueEvent.RecreateQueue(
                                source = albumWithInfoState.albumWithInfo.data!!.albumInfo.title,
                                clickedTrackIndex = 0,
                                queue = queue
                            )
                        )
                    },
                    onShuffle = {

                    },
                    // TODO: remember to Fix Source
                    onClickAlbumTrack = { index, queue ->
                        mediaControllerViewModel.onQueueEvent(
                            QueueEvent.RecreateQueue(
                                source = albumWithInfoState.albumWithInfo.data!!.albumInfo.title,
                                clickedTrackIndex = index,
                                queue = queue
                            )
                        )
                    },
                    onToggleFavorite = { isFavorite, albumHash ->

                    }
                )
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
    fontScale = 1F
)
@Composable
fun AlbumWithInfoScreenPreview() {
    val trackArtist = TrackArtist(artistHash = "hash-2", name = "Khalid", image = "")
    val albumArtist = Artist(
        name = "Khalid",
        artisthash = "hash",
        colors = emptyList(),
        createdDate = 1.0,
        helpText = "2 days ago",
        image = ""
    )

    val albumInfo = AlbumInfo(
        albumArtists = listOf(albumArtist, albumArtist.copy(name = "Juice Wrld")),
        albumHash = "sincere-album-hash",
        artistHashes = listOf("khalid-hash"),
        baseTitle = "Sincere",
        color = "#FFD700",
        createdDate = 1_693_750_000,
        date = 20240811,
        duration = 2700,
        favUserIds = listOf(),
        genreHashes = "rnb-soul-hash",
        genres = listOf(
            Genre(genreHash = "rnb-hash", name = "R&B"),
            Genre(genreHash = "soul-hash", name = "Soul")
        ),
        id = 1,
        image = "https://example.com/sincere-cover.jpg",
        isFavorite = false,
        lastPlayed = 0,
        ogTitle = "Sincere",
        playCount = 0,
        playDuration = 2700,
        title = "Sincere",
        trackCount = 12,
        type = "album",
        versions = listOf("Deluxe", "Standard")
    )

    val tracks = listOf(
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = "khalid-hash",
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 210,
            filepath = "/music/Khalid/Sincere/track1.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track1.jpg",
            isFavorite = true,
            title = "Intro",
            trackHash = "track1-hash"
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = "khalid-hash",
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 180,
            filepath = "/music/Khalid/Sincere/track2.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track2.jpg",
            isFavorite = false,
            title = "Sincere Love",
            trackHash = "track2-hash"
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = "khalid-hash",
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 320,
            filepath = "/music/Khalid/Sincere/track2.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track2.jpg",
            isFavorite = false,
            title = "No Kugeria Maani",
            trackHash = "track2-hash2"
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = "khalid-hash",
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 240,
            filepath = "/music/Khalid/Sincere/track3.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track3.jpg",
            isFavorite = true,
            title = "The Journey",
            trackHash = "track3-hash"
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(),
            albumHash = "sincere-album-hash",
            artistHashes = "khalid-hash",
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 195,
            filepath = "/music/Khalid/Sincere/track4.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track4.jpg",
            isFavorite = false,
            title = "Echoes",
            trackHash = "track4-hash"
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = "khalid-hash",
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 225,
            filepath = "/music/Khalid/Sincere/track5.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track5.jpg",
            isFavorite = true,
            title = "Goodbye",
            trackHash = "track5-hash"
        )
    )

    SwingMusicTheme_Preview {
        AlbumWithInfo(
            currentTrack = tracks[2],
            playbackState = PlaybackState.PLAYING,
            albumInfo = albumInfo,
            albumTracks = tracks,
            baseUrl = "",
            onClickBack = {},
            onClickMore = {},
            onClickArtist = { artistHash -> },
            onClickAlbumTrack = { index, queue -> },
            onPlay = { queue -> },
            onShuffle = {},
            onToggleFavorite = { isFavorite, albumHash -> }
        )
    }
}

private fun Int.toYear(): String {
    val instant = Instant.ofEpochSecond(this.toLong())
    val year = instant.atZone(ZoneOffset.UTC).year
    return year.toString()
}
