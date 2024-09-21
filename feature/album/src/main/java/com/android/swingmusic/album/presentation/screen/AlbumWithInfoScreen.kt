package com.android.swingmusic.album.presentation.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.android.swingmusic.uicomponent.presentation.util.BlurTransformation
import com.android.swingmusic.uicomponent.presentation.util.formatDate
import com.android.swingmusic.uicomponent.presentation.util.formattedAlbumDuration
import com.ramcosta.composedestinations.annotation.Destination

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AlbumWithInfo(
    currentTrack: Track?,
    sortedTracks: List<Track>,
    playbackState: PlaybackState,
    albumInfo: AlbumInfo,
    copyright: String,
    albumTracks: Map<Int, List<Track>>,
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

    Scaffold {
        LazyColumn(
            modifier = Modifier
                .padding()
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            state = listState,
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .fillParentMaxHeight(.5F),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("${baseUrl}img/thumbnail/${albumInfo.image}")
                            .crossfade(true)
                            .transformations(
                                listOf(
                                    BlurTransformation(
                                        scale = 0.25f,
                                        radius = 25
                                    )
                                )
                            )
                            .build(),
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
                                        MaterialTheme.colorScheme.surface.copy(alpha = .25F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .35F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .45F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .65F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .8F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .9F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .95F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 1F)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.fillParentMaxWidth(),
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

                            // TODO: Show album menu
                            /*IconButton(
                                modifier = Modifier.clip(CircleShape),
                                onClick = {
                                    onClickMore()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Back Arrow"
                                )
                            }*/
                        }

                        AsyncImage(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .size(220.dp)
                                .clip(RoundedCornerShape(8))
                                .shadow(elevation = 12.dp)
                                .border(
                                    width = (.5).dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1F),
                                    shape = RoundedCornerShape(8)
                                ),
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
                                    text = albumInfo.type.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
                                        )
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
                                    Text(text = ", ")
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
                                    text = albumInfo.date.formatDate("yyyy"),
                                    style = MaterialTheme.typography.bodySmall,
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

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                val icon = if (albumInfo.isFavorite) R.drawable.fav_filled
                                else R.drawable.fav_not_filled
                                IconButton(
                                    onClick = {
                                        onToggleFavorite(
                                            albumInfo.isFavorite,
                                            albumInfo.albumHash
                                        )
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = "Favorite"
                                    )
                                }
                            }

                            item {
                                IconButton(onClick = {
                                    onShuffle()
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.shuffle),
                                        contentDescription = "Play Icon"
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    onClick = {
                                        onPlay(sortedTracks)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.play_arrow_fill),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        contentDescription = "Play Icon"
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

            // TODO: Edit to scroll to the Playing Track if this Album is the source
            // of the currently playing track...
            // Alternatively, scroll once (if applicable) then preserve the UI state after navigation.

            albumTracks.forEach { (discNumber, tracks) ->
                item {
                    // Disc Number Header
                    Text(
                        text = "Disc $discNumber",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .85F),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                items(
                    items = tracks,
                    key = { item: Track -> item.filepath }
                ) { track ->
                    // Numbered Track Item
                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .then(
                                if (track.trackHash == currentTrack?.trackHash) {
                                    Modifier.padding(horizontal = 12.dp)
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(12))
                            .background(
                                if (track.trackHash == currentTrack?.trackHash) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = .14F)
                                } else {
                                    Color.Unspecified
                                }
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                start =
                                if (track.trackHash != currentTrack?.trackHash) 12.dp else 0.dp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .85F),
                            text = track.trackNumber.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Box(
                            modifier = Modifier.offset((-4).dp)
                        ) {
                            TrackItem(
                                track = track,
                                isAlbumTrack = true,
                                isCurrentTrack = track.trackHash == currentTrack?.trackHash,
                                playbackState = playbackState,
                                onClickTrackItem = {
                                    val trackIndex = sortedTracks.indexOf(track)
                                    if (trackIndex != -1) {
                                        onClickAlbumTrack(trackIndex, sortedTracks)
                                    }
                                },
                                onClickMoreVert = {
                                    // Show menu for Album Track
                                },
                                baseUrl = baseUrl
                            )
                        }
                    }

                    if (track.filepath == sortedTracks.last().filepath) {
                        LazyRow(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            item {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(end = 12.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onSurface
                                        )
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                ) {
                                    Text(
                                        text = when {
                                            albumInfo.genres.size > 1 -> "Genres"
                                            albumInfo.genres.size == 1 -> "Genre"
                                            else -> "No Genres"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.surface
                                    )
                                }
                            }

                            items(albumInfo.genres) { genre ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )

                                ) {
                                    Text(
                                        text = genre.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = albumInfo.date.formatDate("MMMM d, yyyy").uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
                                        )
                                )
                                Text(
                                    text = albumInfo.trackCount.formattedTrackCount().uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = .75F)
                                        )
                                )
                                Text(
                                    text = albumInfo.duration.formattedAlbumDuration().uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                text = copyright.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(150.dp))
                    }
                }
            }
        }
    }
}

@Destination
@Composable
fun AlbumWithInfoScreen(
    albumWithInfoViewModel: AlbumWithInfoViewModel,
    mediaControllerViewModel: MediaControllerViewModel,
    albumNavigator: AlbumNavigator,
    albumHash: String,
) {
    val albumWithInfoState by albumWithInfoViewModel.albumWithInfoState

    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val baseUrl by mediaControllerViewModel.baseUrl.collectAsState()

    LaunchedEffect(key1 = true) {
        if (albumWithInfoState.infoResource !is Resource.Success) {
            albumWithInfoViewModel.onAlbumWithInfoUiEvent(
                AlbumWithInfoUiEvent.OnLoadAlbumWithInfo(albumHash)
            )
        }
    }

    SwingMusicTheme {
        when (albumWithInfoState.infoResource) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Read this from settings
                    val shimmer = false
                    if (shimmer) {
                        ShimmerLoadingAlbumScreen()
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }

            is Resource.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = albumWithInfoState.infoResource.message
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
                    sortedTracks = albumWithInfoState.orderedTracks,
                    playbackState = playerUiState.playbackState,
                    albumInfo = albumWithInfoState.infoResource.data?.albumInfo!!,
                    copyright = albumWithInfoState.infoResource.data?.copyright!!,
                    albumTracks = albumWithInfoState.infoResource.data!!.groupedTracks,
                    baseUrl = baseUrl ?: "https://default",
                    onClickBack = { albumNavigator.navigateBack() },
                    onClickMore = {

                    },
                    onClickArtist = { artistHash ->

                    },
                    onPlay = { queue ->
                        if (queue.isNotEmpty()) {
                            mediaControllerViewModel.onQueueEvent(
                                QueueEvent.RecreateQueue(
                                    source = QueueSource.ALBUM(
                                        albumHash = albumHash,
                                        name = albumWithInfoState.infoResource.data?.albumInfo?.title
                                            ?: ""
                                    ),
                                    clickedTrackIndex = 0,
                                    queue = queue
                                )
                            )
                        }
                    },
                    onShuffle = {
                        if (albumWithInfoState.orderedTracks.isNotEmpty()) {
                            mediaControllerViewModel.initQueueFromAlbum(
                                albumTracks = albumWithInfoState.orderedTracks,
                                albumHash = albumHash,
                                name = albumWithInfoState.infoResource.data?.albumInfo?.title ?: ""
                            )

                            mediaControllerViewModel.onPlayerUiEvent(
                                PlayerUiEvent.OnToggleShuffleMode(
                                    isAlbumSource = true
                                )
                            )
                        }
                    },
                    onClickAlbumTrack = { index, queue ->
                        mediaControllerViewModel.onQueueEvent(
                            QueueEvent.RecreateQueue(
                                source = QueueSource.ALBUM(
                                    albumHash,
                                    name = albumWithInfoState.infoResource.data?.albumInfo?.title
                                        ?: ""
                                ),
                                clickedTrackIndex = index,
                                queue = queue
                            )
                        )
                    },
                    onToggleFavorite = { isFavorite, albumHash ->
                        albumWithInfoViewModel.onAlbumWithInfoUiEvent(
                            AlbumWithInfoUiEvent.OnToggleAlbumFavorite(
                                isFavorite,
                                albumHash
                            )
                        )
                    }
                )
            }
        }
    }
}

private fun Int.formattedTrackCount(): String {
    return when {
        this == 1 -> "$this Track"
        else -> "$this Tracks"
    }
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
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
        albumArtists = listOf(albumArtist),
        albumHash = "sincere-album-hash",
        artistHashes = listOf("khalid-hash"),
        baseTitle = "Sincere",
        color = "#FFD700",
        createdDate = 1_693_750_000,
        date = 1699008919,
        duration = 2716,
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
            artistHashes = listOf("khalid-hash"),
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 210,
            filepath = "/music/Khalid/Sincere/track1.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track1.jpg",
            isFavorite = true,
            title = "Intro",
            trackHash = "track1-hash",
            disc = 1,
            trackNumber = 1
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = listOf("khalid-hash"),
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 180,
            filepath = "/music/Khalid/Sincere/track2.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track2.jpg",
            isFavorite = false,
            title = "Sincere Love",
            trackHash = "track2-hash",
            disc = 1,
            trackNumber = 2
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = listOf("khalid-hash"),
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 320,
            filepath = "/music/Khalid/Sincere/track2-1.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track2.jpg",
            isFavorite = false,
            title = "No Kugeria Maani",
            trackHash = "track2-hash2",
            disc = 1,
            trackNumber = 3
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = listOf("khalid-hash"),
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 240,
            filepath = "/music/Khalid/Sincere/track3.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track3.jpg",
            isFavorite = true,
            title = "The Journey",
            trackHash = "track3-hash",
            disc = 1,
            trackNumber = 4
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(),
            albumHash = "sincere-album-hash",
            artistHashes = listOf("khalid-hash"),
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 195,
            filepath = "/music/Khalid/Sincere/track4.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track4.jpg",
            isFavorite = false,
            title = "Echoes",
            trackHash = "track4-hash",
            disc = 1,
            trackNumber = 5
        ),
        Track(
            album = "Sincere",
            albumTrackArtists = listOf(trackArtist),
            albumHash = "sincere-album-hash",
            artistHashes = listOf("khalid-hash"),
            trackArtists = listOf(trackArtist),
            bitrate = 320,
            duration = 225,
            filepath = "/music/Khalid/Sincere/track5.mp3",
            folder = "/music/Khalid/Sincere",
            image = "https://example.com/sincere-track5.jpg",
            isFavorite = true,
            title = "Goodbye",
            trackHash = "track5-hash",
            disc = 1,
            trackNumber = 6
        )
    )

    SwingMusicTheme_Preview {
        AlbumWithInfo(
            currentTrack = tracks[5],
            sortedTracks = tracks,
            playbackState = PlaybackState.PLAYING,
            albumInfo = albumInfo,
            copyright = "© 2018 Republic Records",
            albumTracks = mapOf(
                1 to listOf(tracks[1], tracks[5])
            ),
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

@Composable
fun ShimmerLoadingAlbumScreen() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05F),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.11F),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07F),
    )

    val transition = rememberInfiniteTransition(label = "album art shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "album art anim"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(top = 72.dp)
                .size(220.dp)
                .clip(RoundedCornerShape(8))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(2))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .height(12.dp)
                .fillMaxWidth(.75F)
                .clip(RoundedCornerShape(2))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(50.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .fillMaxWidth(.25F)
                    .clip(RoundedCornerShape(2))
                    .background(brush)
            )
        }

        repeat(3) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2))
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(1F)
                    .clip(RoundedCornerShape(2))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(1F)
                    .clip(RoundedCornerShape(2))
                    .background(brush)
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ShimmerPreview() {
    SwingMusicTheme_Preview {
        //  ShimmerLoadingAlbumScreen()
    }
}
