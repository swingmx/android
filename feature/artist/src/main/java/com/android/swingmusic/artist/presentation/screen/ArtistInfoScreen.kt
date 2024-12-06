package com.android.swingmusic.artist.presentation.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.swingmusic.artist.presentation.event.ArtistInfoUiEvent
import com.android.swingmusic.artist.presentation.viewmodel.ArtistInfoViewModel
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.AlbumsAndAppearances
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.ArtistExpanded
import com.android.swingmusic.core.domain.model.ArtistInfo
import com.android.swingmusic.core.domain.model.BottomSheetItemModel
import com.android.swingmusic.core.domain.model.Genre
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.BottomSheetAction
import com.android.swingmusic.core.domain.util.PlaybackState
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.player.presentation.event.PlayerUiEvent
import com.android.swingmusic.player.presentation.event.QueueEvent
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.component.AlbumItem
import com.android.swingmusic.uicomponent.presentation.component.ArtistItem
import com.android.swingmusic.uicomponent.presentation.component.CustomTrackBottomSheet
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme_Preview
import com.android.swingmusic.uicomponent.presentation.util.Screen
import com.android.swingmusic.uicomponent.presentation.util.formattedAlbumDuration
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun ArtistInfo(
    baseUrl: String,
    artistInfo: ArtistInfo,
    similarArtists: List<Artist>,
    playbackState: PlaybackState,
    currentTrack: Track?,
    onToggleFavorite: (String, Boolean) -> Unit,
    onShuffle: () -> Unit,
    onPlayAllTracks: () -> Unit,
    onClickBack: () -> Unit,
    onClickAlbum: (albumHash: String) -> Unit,
    onClickArtistTrack: (queue: List<Track>, index: Int) -> Unit,
    onClickSimilarArtist: (artistHash: String) -> Unit,
    onClickViewAll: (artistName: String, viewAllType: String, baseUrl: String) -> Unit,
    onGetSheetAction: (track: Track, sheetAction: BottomSheetAction) -> Unit,
    onGotoArtist: (hash: String) -> Unit
) {
    val clickInteractionSource = remember { MutableInteractionSource() }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showTrackBottomSheet by remember { mutableStateOf(false) }
    var clickedTrack: Track? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
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
                    onClick = {
                        onClickBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Arrow"
                    )
                }
            }
        }
    ) {
        if (showTrackBottomSheet) {
            clickedTrack?.let { track ->
                CustomTrackBottomSheet(
                    scope = scope,
                    sheetState = sheetState,
                    clickedTrack = track,
                    baseUrl = baseUrl,
                    currentArtisthash = artistInfo.artist.artistHash,
                    bottomSheetItems = listOf(
                        BottomSheetItemModel(
                            label = "Go to Artist",
                            enabled = artistInfo.artist.artistHash != track.trackHash && track.trackArtists.size != 1,
                            painterId = R.drawable.ic_artist,
                            track = track,
                            sheetAction = BottomSheetAction.OpenArtistsDialog(track.trackArtists)
                        ),
                        BottomSheetItemModel(
                            label = "Go to Album",
                            enabled = true,
                            painterId = R.drawable.ic_album,
                            track = track,
                            sheetAction = BottomSheetAction.GotoAlbum
                        ),
                        BottomSheetItemModel(
                            label = "Go to Folder",
                            enabled = true,
                            painterId = R.drawable.folder_outlined_open,
                            track = track,
                            sheetAction = BottomSheetAction.GotoFolder(
                                name = track.folder.getFolderName(),
                                path = track.folder
                            )
                        )
                    ),
                    onHideBottomSheet = {
                        showTrackBottomSheet = it
                    },
                    onClickSheetItem = { sheetTrack, sheetAction ->
                        onGetSheetAction(sheetTrack, sheetAction)
                    },
                    onChooseArtist = { hash ->
                        onGotoArtist(hash)
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .fillParentMaxHeight(.5F),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("${baseUrl}img/artist/${artistInfo.artist.image}")
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
                                        MaterialTheme.colorScheme.surface.copy(alpha = .15F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .25F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .35F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .45F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .6F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .9F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = .95F),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 1F)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 250.dp, start = 8.dp, end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Artist",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = artistInfo.artist.name,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillParentMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = artistInfo.artist.trackCount.artistTracksCountHelperText(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9F)
                            )

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(CircleShape)
                                    .size(4.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .5F))
                            )

                            Text(
                                text = artistInfo.artist.albumCount.artistAlbumsCountHelperText(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9F)
                            )

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(CircleShape)
                                    .size(4.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .5F))
                            )

                            Text(
                                text = artistInfo.artist.duration.formattedAlbumDuration(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9F)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                val icon = if (artistInfo.artist.isFavorite) R.drawable.fav_filled
                                else R.drawable.fav_not_filled
                                IconButton(
                                    onClick = {
                                        onToggleFavorite(
                                            artistInfo.artist.artistHash,
                                            artistInfo.artist.isFavorite
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
                                        .clip(RoundedCornerShape(32))
                                        .background(MaterialTheme.colorScheme.primary),
                                    onClick = {
                                        onPlayAllTracks()
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

            if (artistInfo.tracks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 24.dp, bottom = 4.dp, start = 20.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tracks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        if (artistInfo.tracks.size > 4) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(
                                        interactionSource = clickInteractionSource,
                                        indication = null
                                    ) {
                                        onClickViewAll(artistInfo.artist.name, "Tracks", baseUrl)
                                    }
                            ) {
                                Text(
                                    text = "View All",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9F)
                                )
                            }
                        }
                    }
                }
            }

            itemsIndexed(
                items = artistInfo.tracks.take(4),
                key = { index: Int, item: Track -> item.filepath + index }
            ) { index, track ->
                TrackItem(
                    track = track,
                    showMenuIcon = true,
                    baseUrl = baseUrl,
                    isCurrentTrack = track.trackHash == currentTrack?.trackHash,
                    playbackState = playbackState,
                    onClickTrackItem = {
                        onClickArtistTrack(
                            artistInfo.tracks,
                            index
                        )
                    },
                    onClickMoreVert = { trackClicked ->
                        clickedTrack = trackClicked
                        showTrackBottomSheet = true
                    }
                )
            }

            if (artistInfo.albumsAndAppearances.albums.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 24.dp, bottom = 4.dp, start = 20.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Albums",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        if (artistInfo.albumsAndAppearances.albums.size > 3) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(
                                        interactionSource = clickInteractionSource,
                                        indication = null
                                    ) {
                                        onClickViewAll(artistInfo.artist.name, "Albums", baseUrl)
                                    }
                            ) {
                                Text(
                                    text = "View All",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9F)
                                )
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    items(
                        items = artistInfo.albumsAndAppearances.albums
                    ) { album ->
                        Box(modifier = Modifier.width(170.dp)) {
                            AlbumItem(
                                modifier = Modifier.fillMaxWidth(),
                                screen = Screen.ARTIST,
                                albumArtistHash = artistInfo.artist.artistHash,
                                album = album,
                                baseUrl = baseUrl,
                                onClick = {
                                    onClickAlbum(it)
                                }
                            )
                        }
                    }
                }
            }

            if (artistInfo.albumsAndAppearances.singlesAndEps.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp, start = 20.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EP & Singles",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        if (artistInfo.albumsAndAppearances.singlesAndEps.size > 3) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(
                                        interactionSource = clickInteractionSource,
                                        indication = null
                                    ) {
                                        onClickViewAll(
                                            artistInfo.artist.name,
                                            "Ep & Singles",
                                            baseUrl
                                        )
                                    }
                            ) {
                                Text(
                                    text = "View All",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9F)
                                )
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    items(
                        items = artistInfo.albumsAndAppearances.singlesAndEps
                    ) { album ->
                        Box(modifier = Modifier.width(170.dp)) {
                            AlbumItem(
                                modifier = Modifier.fillMaxWidth(),
                                screen = Screen.ARTIST,
                                albumArtistHash = artistInfo.artist.artistHash,
                                album = album,
                                baseUrl = baseUrl,
                                onClick = {
                                    onClickAlbum(it)
                                }
                            )
                        }
                    }
                }
            }

            if (artistInfo.albumsAndAppearances.compilations.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp, start = 20.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Compilations",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        if (artistInfo.albumsAndAppearances.compilations.size > 3) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(
                                        interactionSource = clickInteractionSource,
                                        indication = null
                                    ) {
                                        onClickViewAll(
                                            artistInfo.artist.name,
                                            "Compilations",
                                            baseUrl
                                        )
                                    }
                            ) {
                                Text(
                                    text = "View All",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9F)
                                )
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    items(
                        items = artistInfo.albumsAndAppearances.compilations
                    ) { album ->
                        Box(modifier = Modifier.width(170.dp)) {
                            AlbumItem(
                                modifier = Modifier.fillMaxWidth(),
                                screen = Screen.ARTIST,
                                albumArtistHash = artistInfo.artist.artistHash,
                                album = album,
                                baseUrl = baseUrl,
                                onClick = {
                                    onClickAlbum(it)
                                }
                            )
                        }
                    }
                }
            }

            if (artistInfo.albumsAndAppearances.appearances.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp, start = 20.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Appearances",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (artistInfo.albumsAndAppearances.appearances.size > 3) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .1F)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(
                                        interactionSource = clickInteractionSource,
                                        indication = null
                                    ) {
                                        onClickViewAll(
                                            artistInfo.artist.name,
                                            "Appearances",
                                            baseUrl
                                        )
                                    }
                            ) {
                                Text(
                                    text = "View All",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9F)
                                )
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    items(
                        items = artistInfo.albumsAndAppearances.appearances
                    ) { album ->
                        Box(modifier = Modifier.width(170.dp)) {
                            AlbumItem(
                                modifier = Modifier.fillMaxWidth(),
                                screen = Screen.ARTIST,
                                albumArtistHash = artistInfo.artist.artistHash,
                                showDate = false,
                                album = album,
                                baseUrl = baseUrl,
                                onClick = {
                                    onClickAlbum(it)
                                }
                            )
                        }
                    }
                }
            }

            if (similarArtists.isNotEmpty()) {
                item {
                    Spacer(
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            item {
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
                                    artistInfo.artist.genres.size > 1 -> "Genres"
                                    artistInfo.artist.genres.size == 1 -> "Genre"
                                    else -> "No Genres"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                    }

                    items(artistInfo.artist.genres) { genre ->
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
            }

            if (similarArtists.isNotEmpty()) {
                item {
                    Text(
                        text = "More Like ${artistInfo.artist.name}",
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 16.dp, start = 20.dp)
                    )
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    items(
                        items = similarArtists
                    ) { artist ->
                        Box(modifier = Modifier.width(170.dp)) {
                            ArtistItem(
                                modifier = Modifier.fillMaxWidth(),
                                artist = artist,
                                baseUrl = baseUrl,
                                onClick = { artistHash ->
                                    onClickSimilarArtist(artistHash)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ArtistInfoScreen(
    mediaControllerViewModel: MediaControllerViewModel,
    artistInfoViewModel: ArtistInfoViewModel,
    artistHash: String,
    loadNewArtist: Boolean,
    commonNavigator: CommonNavigator
) {
    val baseUrl = mediaControllerViewModel.baseUrl
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val artistInfoState = artistInfoViewModel.artistInfoState.collectAsState()
    val currentArtistHash = artistInfoState.value.infoResource.data?.artist?.artistHash
    val similarArtists = if (artistInfoState.value.similarArtistsResource is Resource.Success)
        artistInfoState.value.similarArtistsResource.data else emptyList()

    var showOnRefreshIndicator by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

    LaunchedEffect(key1 = Unit) {
        if (artistInfoState.value.requiresReload || loadNewArtist) {
            if (currentArtistHash != artistHash) {
                artistInfoViewModel.onArtistInfoUiEvent(
                    ArtistInfoUiEvent.OnLoadArtistInfo(artistHash)
                )
            }
        }
    }

    SwingMusicTheme {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = showOnRefreshIndicator,
            state = refreshState,
            onRefresh = {
                showOnRefreshIndicator = true

                artistInfoViewModel.onArtistInfoUiEvent(
                    ArtistInfoUiEvent.OnRefresh(
                        artistHash = artistInfoState.value.infoResource.data?.artist?.artistHash
                            ?: artistHash
                    )
                )
            },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier
                        .padding(top = 76.dp)
                        .align(Alignment.TopCenter),
                    isRefreshing = showOnRefreshIndicator,
                    state = refreshState
                )
            }
        ) {
            when (val res = artistInfoState.value.infoResource) {
                is Resource.Loading -> {
                    if (!showOnRefreshIndicator) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is Resource.Error -> {
                    showOnRefreshIndicator = false

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Failed to fetch artist data")

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    artistInfoViewModel.onArtistInfoUiEvent(
                                        ArtistInfoUiEvent.OnRefresh(
                                            artistHash = artistHash
                                        )
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
                }

                is Resource.Success -> {
                    showOnRefreshIndicator = false

                    ArtistInfo(
                        baseUrl = baseUrl.value ?: "https://default.null",
                        artistInfo = res.data!!,
                        similarArtists = similarArtists ?: emptyList(),
                        playbackState = playerUiState.playbackState,
                        currentTrack = playerUiState.nowPlayingTrack,
                        onClickBack = {
                            commonNavigator.navigateBack()
                        },
                        onToggleFavorite = { artistHash, isFavorite ->
                            artistInfoViewModel.onArtistInfoUiEvent(
                                ArtistInfoUiEvent.OnToggleArtistFavorite(
                                    artistHash = artistHash,
                                    isFavorite = isFavorite
                                )
                            )
                        },
                        onShuffle = {
                            val tracks = artistInfoState.value.infoResource.data?.tracks
                            if (tracks?.isNotEmpty() == true) {
                                mediaControllerViewModel.initQueueFromGivenSource(
                                    tracks = tracks,
                                    source = QueueSource.ARTIST(
                                        artistHash = artistInfoState.value.infoResource.data?.artist?.artistHash
                                            ?: "",
                                        name = artistInfoState.value.infoResource.data?.artist?.name
                                            ?: ""
                                    )
                                )

                                mediaControllerViewModel.onPlayerUiEvent(
                                    PlayerUiEvent.OnToggleShuffleMode()
                                )
                            }
                        },
                        onPlayAllTracks = {
                            val queue = artistInfoState.value.infoResource.data?.tracks
                            if (queue?.isNotEmpty() == true) {
                                mediaControllerViewModel.onQueueEvent(
                                    QueueEvent.RecreateQueue(
                                        source = QueueSource.ARTIST(
                                            artistHash = artistInfoState.value.infoResource.data?.artist?.artistHash
                                                ?: "",
                                            name = artistInfoState.value.infoResource.data?.artist?.name
                                                ?: ""
                                        ),
                                        clickedTrackIndex = 0,
                                        queue = queue
                                    )
                                )
                            }
                        },
                        onClickAlbum = {
                            commonNavigator.gotoAlbumWithInfo(it)
                        },
                        onClickArtistTrack = { queue, index ->
                            mediaControllerViewModel.onQueueEvent(
                                QueueEvent.RecreateQueue(
                                    source = QueueSource.ARTIST(
                                        artistHash = artistInfoState.value.infoResource.data?.artist?.artistHash
                                            ?: "",
                                        name = artistInfoState.value.infoResource.data?.artist?.name
                                            ?: ""
                                    ),
                                    clickedTrackIndex = index,
                                    queue = queue
                                )
                            )
                        },
                        onClickSimilarArtist = {
                            artistInfoViewModel.onArtistInfoUiEvent(
                                ArtistInfoUiEvent.OnUpdateArtistHash(it)
                            )
                        },
                        onClickViewAll = { artistName: String, viewAllType: String, baseUrl: String ->
                            commonNavigator.gotoViewAllScreen(
                                viewAllType = viewAllType,
                                artistName = artistName,
                                baseUrl = baseUrl
                            )
                        },
                        onGetSheetAction = { track, sheetAction ->
                            when (sheetAction) {
                                is BottomSheetAction.GotoAlbum -> {
                                    commonNavigator.gotoAlbumWithInfo(track.albumHash)
                                }

                                is BottomSheetAction.GotoFolder -> {
                                    commonNavigator.gotoSourceFolder(
                                        sheetAction.name,
                                        sheetAction.path
                                    )
                                }

                                else -> {}
                            }
                        },
                        onGotoArtist = { hash ->
                            artistInfoViewModel.onArtistInfoUiEvent(
                                ArtistInfoUiEvent.OnUpdateArtistHash(hash)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArtistInfoPreview() {
    val sampleArtistInfo = ArtistInfo(
        albumsAndAppearances = AlbumsAndAppearances(
            albums = listOf(
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        )
                    ),
                    albumHash = "album_hash_123",
                    colors = listOf("#FF5733", "#C70039"),
                    createdDate = 1625068800.0,
                    date = 2020,
                    helpText = "2020",
                    image = "https://example.com/sample_album_1.jpg",
                    title = "Greatest Hits",
                    versions = listOf("Deluxe", "Standard")
                ),
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        )
                    ),
                    albumHash = "album_hash_124",
                    colors = listOf("#28B463", "#1F618D"),
                    createdDate = 1619827200.0,
                    date = 2018,
                    helpText = "1970",
                    image = "https://example.com/sample_album_2.jpg",
                    title = "Live at the Arena And Some More Info Which Is Large",
                    versions = listOf()
                ),
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        )
                    ),
                    albumHash = "album_hash_124",
                    colors = listOf("#28B463", "#1F618D"),
                    createdDate = 1619827200.0,
                    date = 2018,
                    helpText = "1970",
                    image = "https://example.com/sample_album_2.jpg",
                    title = "Live at the Arena And Some More Info Which Is Large",
                    versions = listOf("Live Edition")
                )
            ),
            appearances = listOf(
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "artist_hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Artist 1"
                        ),
                        Artist(
                            artistHash = "artist_hash_123",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        )
                    ),
                    albumHash = "album_hash_125",
                    colors = listOf("#F1C40F", "#E74C3C"),
                    createdDate = 1622563200.0,
                    date = 2021,
                    helpText = "Yesterday",
                    image = "https://example.com/sample_appearance_album.jpg",
                    title = "Top Collaborations",
                    versions = listOf()
                ),
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        )
                    ),
                    albumHash = "album_hash_125",
                    colors = listOf("#F1C40F", "#E74C3C"),
                    createdDate = 1622563200.0,
                    date = 2021,
                    helpText = "Yesterday",
                    image = "https://example.com/sample_appearance_album.jpg",
                    title = "Top Collaborations",
                    versions = listOf()
                )
            ),
            artistName = "Sample Artist",
            compilations = listOf(
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        )
                    ),
                    albumHash = "album_hash_126",
                    colors = listOf("#9B59B6", "#8E44AD"),
                    createdDate = 1580515200.0,
                    date = 2019,
                    helpText = "10 Tracks",
                    image = "https://example.com/sample_compilation_album.jpg",
                    title = "The Best of Sample",
                    versions = listOf("Comp")
                ),
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        )
                    ),
                    albumHash = "album_hash_126",
                    colors = listOf("#9B59B6", "#8E44AD"),
                    createdDate = 1580515200.0,
                    date = 2019,
                    helpText = "10 Tracks",
                    image = "https://example.com/sample_compilation_album.jpg",
                    title = "The Best of Sample",
                    versions = listOf()
                )
            ),
            singlesAndEps = listOf(
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        )
                    ),
                    albumHash = "album_hash_127",
                    colors = listOf("#2980B9", "#2C3E50"),
                    createdDate = 1640995200.0,
                    date = 2022,
                    helpText = "2022",
                    image = "https://example.com/sample_ep.jpg",
                    title = "Sample EP",
                    versions = emptyList()
                ),
                Album(
                    albumArtists = listOf(
                        Artist(
                            artistHash = "hash",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Sample Artist"
                        ),
                        Artist(
                            artistHash = "hash-1",
                            colors = emptyList(),
                            createdDate = (846384444).toDouble(),
                            helpText = "Sample Artist H",
                            image = "",
                            name = "Artist 2"
                        )
                    ),
                    albumHash = "album_hash_127",
                    colors = listOf("#2980B9", "#2C3E50"),
                    createdDate = 1640995200.0,
                    date = 2022,
                    helpText = "2022",
                    image = "https://example.com/sample_ep.jpg",
                    title = "Sample EP",
                    versions = emptyList()
                )
            )
        ),
        artist = ArtistExpanded(
            albumCount = 10,
            artistHash = "artist_hash_123",
            color = "#FF5733",
            duration = 12040,
            genres = listOf(
                Genre(genreHash = "genre_hash_rock", name = "Rock"),
                Genre(genreHash = "genre_hash_pop", name = "Pop")
            ),
            image = "https://example.com/sample_artist.jpg",
            isFavorite = true,
            name = "Khalid",
            trackCount = 120
        ),
        tracks = listOf(
            Track(
                album = "Greatest Hits",
                albumTrackArtists = listOf(),
                albumHash = "album_hash_123",
                trackArtists = listOf(
                    TrackArtist(artistHash = "hash", image = "", name = "Sample Artist")
                ),
                bitrate = 320,
                duration = 180,
                filepath = "/music/sample_artist/greatest_hits/sample_track_1.mp3",
                folder = "/music/sample_artist/greatest_hits",
                image = "https://example.com/sample_track_1.jpg",
                isFavorite = true,
                title = "Sample Track 1",
                trackHash = "track_hash_12",
                disc = 1,
                trackNumber = 1
            ),
            Track(
                album = "Greatest Hits",
                albumTrackArtists = listOf(),
                albumHash = "album_hash_123",
                trackArtists = listOf(
                    TrackArtist(artistHash = "hash", image = "", name = "Sample Artist")
                ),
                bitrate = 320,
                duration = 180,
                filepath = "/music/sample_artist/greatest_hits/sample_track_2.mp3",
                folder = "/music/sample_artist/greatest_hits",
                image = "https://example.com/sample_track_1.jpg",
                isFavorite = true,
                title = "Sample Track 1",
                trackHash = "track_hash_123",
                disc = 1,
                trackNumber = 1
            ),
            Track(
                album = "Live at the Arena",
                albumTrackArtists = listOf(),
                albumHash = "album_hash_124",
                trackArtists = listOf(
                    TrackArtist(artistHash = "hash", image = "", name = "Sample Artist")
                ),
                bitrate = 320,
                duration = 200,
                filepath = "/music/sample_artist/live_at_the_arena/sample_track_3.mp3",
                folder = "/music/sample_artist/live_at_the_arena",
                image = "https://example.com/sample_track_2.jpg",
                isFavorite = false,
                title = "Sample Track 2",
                trackHash = "track_hash_124",
                disc = 1,
                trackNumber = 2
            ),
            Track(
                album = "Live at the Arena",
                albumTrackArtists = listOf(),
                albumHash = "album_hash_124",
                trackArtists = listOf(
                    TrackArtist(artistHash = "hash", image = "", name = "Sample Artist")
                ),
                bitrate = 320,
                duration = 200,
                filepath = "/music/sample_artist/live_at_the_arena/sample_track_4.mp3",
                folder = "/music/sample_artist/live_at_the_arena",
                image = "https://example.com/sample_track_2.jpg",
                isFavorite = false,
                title = "Sample Track 2",
                trackHash = "track_hash_124",
                disc = 1,
                trackNumber = 2
            ),
            Track(
                album = "Live at the Arena",
                albumTrackArtists = listOf(),
                albumHash = "album_hash_124",
                trackArtists = listOf(
                    TrackArtist(artistHash = "hash", image = "", name = "Sample Artist")
                ),
                bitrate = 320,
                duration = 200,
                filepath = "/music/sample_artist/live_at_the_arena/sample_track_5.mp3",
                folder = "/music/sample_artist/live_at_the_arena",
                image = "https://example.com/sample_track_2.jpg",
                isFavorite = false,
                title = "Sample Track 2",
                trackHash = "track_hash_124",
                disc = 1,
                trackNumber = 2
            ),
            Track(
                album = "Live at the Arena",
                albumTrackArtists = listOf(),
                albumHash = "album_hash_124",
                trackArtists = listOf(
                    TrackArtist(artistHash = "hash", image = "", name = "Sample Artist")
                ),
                bitrate = 320,
                duration = 200,
                filepath = "/music/sample_artist/live_at_the_arena/sample_track_6.mp3",
                folder = "/music/sample_artist/live_at_the_arena",
                image = "https://example.com/sample_track_2.jpg",
                isFavorite = false,
                title = "Sample Track 2",
                trackHash = "track_hash_124",
                disc = 1,
                trackNumber = 2
            )
        )
    )

    SwingMusicTheme_Preview {
        ArtistInfo(
            baseUrl = "",
            artistInfo = sampleArtistInfo,
            playbackState = PlaybackState.PLAYING,
            currentTrack = Track(
                album = "Greatest Hits",
                albumTrackArtists = listOf(),
                albumHash = "album_hash_123",
                trackArtists = listOf(
                    TrackArtist(artistHash = "hash", image = "", name = "Sample Artist")
                ),
                bitrate = 320,
                duration = 180,
                filepath = "/music/sample_artist/greatest_hits/sample_track_2.mp3",
                folder = "/music/sample_artist/greatest_hits",
                image = "https://example.com/sample_track_1.jpg",
                isFavorite = true,
                title = "Sample Track 1",
                trackHash = "track_hash_123",
                disc = 1,
                trackNumber = 1
            ),
            similarArtists = listOf(
                Artist(
                    artistHash = "hash",
                    colors = emptyList(),
                    createdDate = (846384444).toDouble(),
                    helpText = "Sample Artist H",
                    image = "",
                    name = "Sample Artist"
                ),
                Artist(
                    artistHash = "hash",
                    colors = emptyList(),
                    createdDate = (846384444).toDouble(),
                    helpText = "Sample Artist H",
                    image = "",
                    name = "Sample Artist"
                ),
                Artist(
                    artistHash = "hash",
                    colors = emptyList(),
                    createdDate = (846384444).toDouble(),
                    helpText = "Sample Artist H",
                    image = "",
                    name = "Sample Artist"
                ),
            ),
            onClickBack = {},
            onToggleFavorite = { _, _ -> },
            onShuffle = {},
            onPlayAllTracks = {},
            onClickAlbum = {},
            onClickArtistTrack = { _, _ -> },
            onClickSimilarArtist = {},
            onClickViewAll = { _, _, _ -> },
            onGetSheetAction = { _, _ -> },
            onGotoArtist = {}
        )
    }
}

private fun Int.artistTracksCountHelperText(): String {
    return when {
        this == 1 -> "$this Track"
        else -> "$this Tracks"
    }
}

private fun Int.artistAlbumsCountHelperText(): String {
    return when {
        this == 1 -> "$this Album"
        else -> "$this Albums"
    }
}

internal fun String.getFolderName(): String {
    val sanitizedPath = this.trimEnd('/')
    return sanitizedPath.substringAfterLast('/')
}
