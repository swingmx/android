package com.android.swingmusic.library

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.library.presentation.event.PlaylistsUiEffect
import com.android.swingmusic.library.presentation.event.PlaylistsUiEvent
import com.android.swingmusic.library.presentation.viewmodel.PlaylistsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlaylistsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `puts pinned playlists first and keeps each group's order`() {
        val viewModel = PlaylistsViewModel(FakePlaylistRepository())

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingPlaylists)
        assertEquals(listOf("2"), state.pinnedPlaylists.map { it.id })
        assertEquals(listOf("1", "3"), state.otherPlaylists.map { it.id })
    }

    @Test
    fun `creates a playlist with the trimmed name and closes the dialog`() = runTest {
        val repository = FakePlaylistRepository()
        val viewModel = PlaylistsViewModel(repository)

        viewModel.onEvent(PlaylistsUiEvent.OnNewPlaylistClicked)
        viewModel.onEvent(PlaylistsUiEvent.OnNewPlaylistNameChanged("  Road Trip  "))
        viewModel.onEvent(PlaylistsUiEvent.OnCreatePlaylist)

        assertEquals(listOf("Road Trip"), repository.created)
        assertFalse(viewModel.uiState.value.showNewPlaylistDialog)
        assertEquals(PlaylistsUiEffect.ShowSnackbar("Created Road Trip"), viewModel.uiEffect.first())
    }

    @Test
    fun `ignores a blank name`() {
        val repository = FakePlaylistRepository()
        val viewModel = PlaylistsViewModel(repository)

        viewModel.onEvent(PlaylistsUiEvent.OnNewPlaylistClicked)
        viewModel.onEvent(PlaylistsUiEvent.OnNewPlaylistNameChanged("   "))
        viewModel.onEvent(PlaylistsUiEvent.OnCreatePlaylist)

        assertTrue(repository.created.isEmpty())
        assertTrue(viewModel.uiState.value.showNewPlaylistDialog)
    }

    @Test
    fun `keeps the dialog open with the error when the name is taken`() {
        val repository = FakePlaylistRepository(createResult = Resource.Error(message = "A playlist with that name already exists"))
        val viewModel = PlaylistsViewModel(repository)

        viewModel.onEvent(PlaylistsUiEvent.OnNewPlaylistClicked)
        viewModel.onEvent(PlaylistsUiEvent.OnNewPlaylistNameChanged("Focus"))
        viewModel.onEvent(PlaylistsUiEvent.OnCreatePlaylist)

        val state = viewModel.uiState.value
        assertTrue(state.showNewPlaylistDialog)
        assertEquals("A playlist with that name already exists", state.newPlaylistError)
    }
}
