package com.android.swingmusic.artist.presentation.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.android.swingmusic.artist.presentation.event.ArtistInfoUiEvent
import com.android.swingmusic.artist.presentation.viewmodel.ArtistInfoViewModel
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.AlbumsAndAppearances
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.ArtistExpanded
import com.android.swingmusic.core.domain.model.ArtistInfo
import com.android.swingmusic.core.domain.model.StatItem
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
import com.android.swingmusic.uicomponent.presentation.component.ShuffleAndPlayButtons
import com.android.swingmusic.uicomponent.presentation.component.TrackItem
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.BlurTransformation
import com.android.swingmusic.uicomponent.presentation.util.Screen
import com.android.swingmusic.uicomponent.presentation.util.formattedAlbumDuration
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun ArtistInfo(
    baseUrl: String,
    artistInfo: ArtistInfo,
    similarArtists: List<Artist>,
    playbackState: PlaybackState,
    currentTrack: Track?,
    onToggleArtistFavorite: (String, Boolean) -> Unit,
    onToggleTrackFavorite: (trackHash: String, isFavorite: Boolean) -> Unit,
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

    LaunchedEffect(artistInfo.tracks) {
        clickedTrack?.let { track ->
            val updatedTrack = artistInfo.tracks.find { it.trackHash == track.trackHash }
            clickedTrack = updatedTrack ?: track
        }
    }

    Scaffold {
        if (showTrackBottomSheet) {
            clickedTrack?.let { track ->
                CustomTrackBottomSheet(
                    scope = scope,
                    sheetState = sheetState,
                    isFavorite = track.isFavorite,
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
                            painterId = R.drawable.ic_album,
                            track = track,
                            sheetAction = BottomSheetAction.GotoAlbum
                        ),
                        BottomSheetItemModel(
                            label = "Go to Folder",
                            painterId = R.drawable.folder_outlined_open,
                            track = track,
                            sheetAction = BottomSheetAction.GotoFolder(
                                name = track.folder.getFolderName(),
                                path = track.folder
                            )
                        ),
                        BottomSheetItemModel(
                            label = "Play Next",
                            enabled = true,
                            painterId = R.drawable.play_next,
                            track = track,
                            sheetAction = BottomSheetAction.PlayNext
                        ),
                        BottomSheetItemModel(
                            label = "Add to playing queue",
                            painterId = R.drawable.add_to_queue,
                            track = track,
                            sheetAction = BottomSheetAction.AddToQueue
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
                    },
                    onToggleTrackFavorite = { trackHash, isFavorite ->
                        onToggleTrackFavorite(trackHash, isFavorite)
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .fillParentMaxHeight(.5F),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("${baseUrl}img/artist/${artistInfo.artist.image}")
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
                                modifier = Modifier.clip(CircleShape),
                                onClick = { onClickBack() }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back Arrow"
                                )
                            }
                        }

                        AsyncImage(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .size(220.dp)
                                .shadow(elevation = 12.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .border(
                                    width = (.5).dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1F),
                                    shape = CircleShape
                                ),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("${baseUrl}img/artist/${artistInfo.artist.image}")
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.artist_fallback),
                            fallback = painterResource(R.drawable.artist_fallback),
                            error = painterResource(R.drawable.artist_fallback),
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
                            text = artistInfo.artist.name,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            listOf(
                                "Artist",
                                artistInfo.artist.trackCount.artistTracksCountHelperText(),
                                artistInfo.artist.albumCount.artistAlbumsCountHelperText()
                            ).forEachIndexed { index, label ->
                                if (index > 0) {
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
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            text = artistInfo.artist.duration.formattedAlbumDuration(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                val icon = if (artistInfo.artist.isFavorite) R.drawable.fav_filled
                                else R.drawable.fav_not_filled
                                IconButton(
                                    onClick = {
                                        onToggleArtistFavorite(
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
                                ShuffleAndPlayButtons(
                                    onShuffle = { onShuffle() },
                                    onPlay = { onPlayAllTracks() }
                                )
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

            if (artistInfo.stats.isNotEmpty()) {
                item {
                    ArtistStatsSection(stats = artistInfo.stats, baseUrl = baseUrl)
                }
            }

            item {
                Spacer(modifier = Modifier.height(200.dp))
            }
        }
    }
}

@Composable
private fun ArtistStatsSection(stats: List<StatItem>, baseUrl: String) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Stats",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stats) { stat ->
                ArtistStatCard(stat = stat, baseUrl = baseUrl)
            }
        }
    }
}

@Composable
private fun ArtistStatCard(stat: StatItem, baseUrl: String) {
    Column(
        modifier = Modifier
            .size(width = 150.dp, height = 130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(14.dp)
    ) {
        if (stat.image != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${baseUrl}img/thumbnail/small/${stat.image}")
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.audio_fallback),
                fallback = painterResource(R.drawable.audio_fallback),
                error = painterResource(R.drawable.audio_fallback),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1F))
        BasicText(
            text = stat.value,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 13.sp, maxFontSize = 20.sp)
        )
        Text(
            text = stat.text,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun StatItem.icon(): ImageVector = when (type) {
    "play_duration" -> Icons.Rounded.Schedule
    "played" -> Icons.Rounded.PlayCircle
    "toptrack" -> Icons.Rounded.MusicNote
    "topalbum" -> Icons.Rounded.Album
    "completeness" -> Icons.Rounded.CheckCircle
    else -> Icons.Rounded.BarChart
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun ArtistStatsSectionPreview() {
    SwingMusicTheme {
        ArtistStatsSection(
            stats = listOf(
                StatItem(type = "play_duration", value = "28 hrs, 2 mins", text = "listened all time", image = null),
                StatItem(type = "played", value = "9/67 tracks", text = "never played", image = null),
                StatItem(type = "toptrack", value = "Toosie Slide", text = "top track (42 mins listened)", image = "toosie.webp")
            ),
            baseUrl = ""
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ArtistInfoScreen(
    mediaControllerViewModel: MediaControllerViewModel,
    artistInfoViewModel: ArtistInfoViewModel,
    artistHash: String,
    loadNewArtist: Boolean = false,
    commonNavigator: CommonNavigator
) {
    val baseUrl = mediaControllerViewModel.baseUrl
    val playerUiState by mediaControllerViewModel.playerUiState.collectAsState()
    val artistInfoState = artistInfoViewModel.artistInfoState.collectAsState()
    val currentArtistHash = artistInfoState.value.infoResource.data?.artist?.artistHash
    val similarArtists = if (artistInfoState.value.similarArtistsResource is Resource.Success)
        artistInfoState.value.similarArtistsResource.data else emptyList()

    var routeByGotoArtist by remember { mutableStateOf(false) }

    // Shared by the header's back arrow and the system back gesture
    fun navigateBack() {
        if (routeByGotoArtist) {
            commonNavigator.navigateBack()
            return
        }
        if (artistInfoState.value.artistHashBackStack.size <= 1) {
            routeByGotoArtist = false
            commonNavigator.navigateBack()
        }
        artistInfoViewModel.onArtistInfoUiEvent(ArtistInfoUiEvent.OnNavigateBack)
    }

    var showOnRefreshIndicator by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        routeByGotoArtist = loadNewArtist

        val lastHash = artistInfoState.value.artistHashBackStack.lastOrNull() ?: artistHash
        val hashToLoad = if (routeByGotoArtist) artistHash else lastHash
        if (hashToLoad != currentArtistHash) {
            artistInfoViewModel.onArtistInfoUiEvent(
                ArtistInfoUiEvent.OnLoadArtistInfo(hashToLoad)
            )
        }
    }

    SwingMusicTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 170.dp)
                )
            }
        ) {
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
                            onClickBack = { navigateBack() },
                            onToggleArtistFavorite = { artistHash, isFavorite ->
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
                                routeByGotoArtist = false

                                artistInfoViewModel.onArtistInfoUiEvent(
                                    ArtistInfoUiEvent.OnLoadArtistInfo(it)
                                )
                            },
                            onClickViewAll = { artistName: String, viewAllType: String, baseUrl: String ->
                                commonNavigator.gotoViewAllOnArtistScreen(
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

                                    is BottomSheetAction.PlayNext -> {
                                        mediaControllerViewModel.onQueueEvent(
                                            QueueEvent.PlayNext(
                                                track = track,
                                                source = QueueSource.ARTIST(
                                                    artistHash,
                                                    artistInfoState.value.infoResource.data?.artist?.name
                                                        ?: "Artist"
                                                )
                                            )
                                        )

                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Track added to play next",
                                                actionLabel = "View Queue",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                commonNavigator.gotoQueueScreen()
                                            }
                                        }
                                    }

                                    is BottomSheetAction.AddToQueue -> {
                                        mediaControllerViewModel.onQueueEvent(
                                            QueueEvent.AddToQueue(
                                                track = track,
                                                source = QueueSource.ARTIST(
                                                    artistHash,
                                                    artistInfoState.value.infoResource.data?.artist?.name
                                                        ?: "Artist"
                                                )
                                            )
                                        )

                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Track added to playing queue",
                                                actionLabel = "View Queue",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                commonNavigator.gotoQueueScreen()
                                            }
                                        }
                                    }

                                    else -> {}
                                }
                            },
                            onGotoArtist = { hash ->
                                routeByGotoArtist = false

                                artistInfoViewModel.onArtistInfoUiEvent(
                                    ArtistInfoUiEvent.OnLoadArtistInfo(hash)
                                )
                            },
                            onToggleTrackFavorite = { trackHash, isFavorite ->
                                artistInfoViewModel.onArtistInfoUiEvent(
                                    ArtistInfoUiEvent.ToggleArtistTrackFavorite(
                                        trackHash, isFavorite
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    BackHandler(enabled = routeByGotoArtist.not()) { navigateBack() }
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

    SwingMusicTheme {
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
            onToggleArtistFavorite = { _, _ -> },
            onShuffle = {},
            onPlayAllTracks = {},
            onClickAlbum = {},
            onClickArtistTrack = { _, _ -> },
            onClickSimilarArtist = {},
            onClickViewAll = { _, _, _ -> },
            onGetSheetAction = { _, _ -> },
            onGotoArtist = {},
            onToggleTrackFavorite = { _, _ -> }
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
