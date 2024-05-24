package com.android.swingmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.swingmusic.artist.presentation.viewmodel.ArtistsViewModel
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.util.PlayerState
import com.android.swingmusic.core.domain.util.RepeatMode
import com.android.swingmusic.core.domain.util.ShuffleMode
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.player.presentation.FullPlayerScreen
import com.android.swingmusic.player.presentation.NowPlayingScreen
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val foldersViewModel: FoldersViewModel by viewModels<FoldersViewModel>()
    private val artistsViewModel: ArtistsViewModel by viewModels<ArtistsViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwingMusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    // TODO: Add a bottom nav bar and a mini player - visible across the entire app
                    // For interaction purposes
                    // FoldersAndTracksScreen(foldersViewModel)
                    // ArtistsScreen(artistsViewModel)

                    val weeknd = TrackArtist(
                        artistHash = "juice123",
                        image = "juice.jpg",
                        name = "The Weeknd"
                    )

                    val genre = listOf("Rap", "Emo")
                    val track = Track(
                        album = "Sample Album",
                        albumTrackArtists = listOf(),
                        albumHash = "albumHash123",
                        artistHashes = "artistHashes123",
                        trackArtists = listOf(weeknd),
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
                        image = "aefcb0afd5.webp",
                        isFavorite = true,
                        lastMod = 1648731600, // Sample timestamp
                        ogAlbum = "Original Album",
                        ogTitle = "Original Title",
                        pos = 1,
                        title = "Save Your Tears",
                        track = 1,
                        trackHash = "aefcb0afd5"
                    )

                    FullPlayerScreen(
                        track = track,
                        progress = .5F,
                        playerState = PlayerState.PLAYING,
                        repeatMode = RepeatMode.REPEAT_NONE,
                        shuffleMode = ShuffleMode.SHUFFLE_OFF,
                        onClickArtist = {},
                        onToggleRepeatMode = {},
                        onClickPrev = {},
                        onTogglePlayerState = {},
                        onClickNext = {},
                        onToggleShuffleMode = {},
                        onSliderPositionChanged = {},
                        onClickLyrics = {},
                        onToggleFavorite = {},
                        onClickQueue = {},
                        onClickMore = {}
                    )
                    val queue = mutableListOf<Track>(
                        /*track,
                        track.copy(title = "Popular", trackHash = "popular"),
                        track.copy(title = "One Right Now", trackHash = "one")*/
                    )
                    (1..20).forEach {
                        val thisTrack = track.copy(title = "Track $it", trackHash = "one+${it}")
                        queue.add(thisTrack)
                    }

                  /*  NowPlayingScreen(
                        nowPlayingTrackIndex = 5,
                        playerState = PlayerState.PLAYING,
                        queue = queue
                    )*/
                }
            }
        }
    }
}
