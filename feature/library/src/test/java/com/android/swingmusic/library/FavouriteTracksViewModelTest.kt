package com.android.swingmusic.library

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.library.presentation.event.FavouriteTracksUiEffect
import com.android.swingmusic.library.presentation.event.FavouriteTracksUiEvent
import com.android.swingmusic.library.presentation.viewmodel.FavouriteTracksViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavouriteTracksViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(favourites: FakeFavouritesRepository) =
        FavouriteTracksViewModel(FakeLibraryRepository(), favourites)

    @Test
    fun `loads the first page with its total`() {
        val viewModel = viewModel(FakeFavouritesRepository())

        val state = viewModel.uiState.value
        assertEquals(listOf("a", "b", "c"), state.tracks.map { it.trackHash })
        assertEquals(3, state.total)
        assertTrue(state.endReached)
    }

    @Test
    fun `unfavouriting removes the track and offers undo`() = runTest {
        val favourites = FakeFavouritesRepository()
        val viewModel = viewModel(favourites)

        viewModel.onEvent(FavouriteTracksUiEvent.OnUnfavouriteTrack(track("b")))

        assertEquals(listOf("a", "c"), viewModel.uiState.value.tracks.map { it.trackHash })
        assertEquals(2, viewModel.uiState.value.total)
        assertEquals(listOf("b" to false), favourites.setFavouriteCalls)
        assertEquals(
            FavouriteTracksUiEffect.ShowSnackbar("Removed from favourites", undoable = true),
            viewModel.uiEffect.first()
        )
    }

    @Test
    fun `undo puts the track back where it was and favourites it again`() {
        val favourites = FakeFavouritesRepository()
        val viewModel = viewModel(favourites)

        viewModel.onEvent(FavouriteTracksUiEvent.OnUnfavouriteTrack(track("b")))
        viewModel.onEvent(FavouriteTracksUiEvent.OnUndoUnfavourite)

        assertEquals(listOf("a", "b", "c"), viewModel.uiState.value.tracks.map { it.trackHash })
        assertEquals(3, viewModel.uiState.value.total)
        assertEquals(listOf("b" to false, "b" to true), favourites.setFavouriteCalls)
    }

    @Test
    fun `a failed unfavourite restores the track`() = runTest {
        val favourites = FakeFavouritesRepository(setFavouriteResult = Resource.Error(message = "offline"))
        val viewModel = viewModel(favourites)

        viewModel.onEvent(FavouriteTracksUiEvent.OnUnfavouriteTrack(track("b")))

        assertEquals(listOf("a", "b", "c"), viewModel.uiState.value.tracks.map { it.trackHash })
        assertEquals(FavouriteTracksUiEffect.ShowSnackbar("offline"), viewModel.uiEffect.first())
    }

    @Test
    fun `playing from a track queues every favourite from that index`() = runTest {
        val viewModel = viewModel(FakeFavouritesRepository())

        viewModel.onEvent(FavouriteTracksUiEvent.OnTrackClicked(index = 1))

        val effect = viewModel.uiEffect.first() as FavouriteTracksUiEffect.PlayTracks
        assertEquals(listOf("a", "b", "c"), effect.tracks.map { it.trackHash })
        assertEquals(1, effect.startIndex)
    }
}
