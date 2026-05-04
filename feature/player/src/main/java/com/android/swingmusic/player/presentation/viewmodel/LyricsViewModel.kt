package com.android.swingmusic.player.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Lyrics
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.player.domain.repository.LyricsRepository
import com.android.swingmusic.player.presentation.event.LyricsUiEvent
import com.android.swingmusic.player.presentation.state.LyricsUiState
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val lyricsRepository: LyricsRepository,
    private val settings: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LyricsUiState())
    val state: StateFlow<LyricsUiState> get() = _state

    private var advanceJob: Job? = null
    private var fetchJob: Job? = null

    fun onEvent(event: LyricsUiEvent) {
        when (event) {
            is LyricsUiEvent.LoadLyrics -> loadLyrics(event.track)
            is LyricsUiEvent.PositionChanged -> onPosition(event.positionMs)
            is LyricsUiEvent.SetUserScrolled -> _state.update { it.copy(userScrolled = event.value) }
            is LyricsUiEvent.SearchOnline -> searchOnline(event.track)
        }
    }

    private fun loadLyrics(track: Track) {
        if (track.trackHash == _state.value.trackHash && _state.value.lines.isNotEmpty()) return

        cancelTimers()
        _state.update {
            LyricsUiState(
                isLoading = true,
                trackHash = track.trackHash
            )
        }

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            lyricsRepository.getLyrics(track.filepath, track.trackHash).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }

                    is Resource.Success -> {
                        val lyrics: Lyrics = resource.data ?: Lyrics(true, emptyList(), "", false)
                        _state.update {
                            it.copy(
                                lines = lyrics.lines,
                                synced = lyrics.synced,
                                exists = lyrics.exists,
                                copyright = lyrics.copyright,
                                isLoading = false,
                                errorMessage = null,
                                currentLine = -1
                            )
                        }
                        maybeAutoSearchOnline(track, lyrics)
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                lines = emptyList(),
                                exists = false,
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                        maybeAutoSearchOnline(track, null)
                    }
                }
            }
        }
    }

    private suspend fun maybeAutoSearchOnline(track: Track, lyrics: Lyrics?) {
        val pluginEnabled = settings.useLyricsPlugin.first()
        if (!pluginEnabled) return

        val autoDownload = settings.lyricsAutoDownload.first()
        val overrideUnsynced = settings.lyricsOverrideUnsynced.first()

        val noLocalLyrics = lyrics == null || !lyrics.exists || lyrics.lines.isEmpty()
        val unsyncedAndOverride = lyrics != null && lyrics.exists && !lyrics.synced && overrideUnsynced

        if ((noLocalLyrics && autoDownload) || unsyncedAndOverride) {
            searchOnline(track)
        }
    }

    private fun searchOnline(track: Track) {
        if (_state.value.pluginSearching) return
        _state.update { it.copy(pluginSearching = true, pluginError = null) }

        viewModelScope.launch {
            val artistName = track.trackArtists.joinToString(", ") { it.name }
            lyricsRepository.searchLyricsOnline(
                trackHash = track.trackHash,
                title = track.title,
                artist = artistName,
                filepath = track.filepath,
                album = track.album
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit

                    is Resource.Success -> {
                        if (track.trackHash != _state.value.trackHash) {
                            _state.update { it.copy(pluginSearching = false) }
                            return@collect
                        }
                        val lyrics = resource.data ?: return@collect
                        _state.update {
                            it.copy(
                                lines = lyrics.lines,
                                synced = lyrics.synced,
                                exists = true,
                                copyright = lyrics.copyright,
                                pluginSearching = false,
                                pluginError = null,
                                errorMessage = null,
                                currentLine = -1
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                pluginSearching = false,
                                pluginError = resource.message
                            )
                        }
                        delay(5_000)
                        _state.update { it.copy(pluginError = null) }
                    }
                }
            }
        }
    }

    private fun onPosition(positionMs: Long) {
        val s = _state.value
        if (!s.exists || !s.synced || s.lines.isEmpty()) return

        val newLine = calculateLineIndex(s.lines, positionMs)
        if (newLine != s.currentLine) {
            advanceJob?.cancel()
            _state.update { it.copy(currentLine = newLine) }
        }

        scheduleNextLine(positionMs)
    }

    private fun scheduleNextLine(positionMs: Long) {
        val s = _state.value
        val nextIndex = s.currentLine + 1
        if (nextIndex !in s.lines.indices) return
        val nextTime = s.lines[nextIndex].time
        val diff = nextTime - positionMs
        if (diff !in 0..1200) return
        if (advanceJob?.isActive == true) return

        advanceJob = viewModelScope.launch {
            val sleep = (diff - 300).coerceAtLeast(0)
            delay(sleep)
            val current = _state.value
            if (current.trackHash != s.trackHash) return@launch
            val next = current.currentLine + 1
            if (next in current.lines.indices) {
                _state.update { it.copy(currentLine = next) }
            }
        }
    }

    private fun calculateLineIndex(lines: List<com.android.swingmusic.core.domain.model.LyricsLine>, positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        var idx = -1
        for (i in lines.indices) {
            if (lines[i].time <= positionMs) idx = i else break
        }
        return idx
    }

    private fun cancelTimers() {
        advanceJob?.cancel()
        advanceJob = null
    }

    override fun onCleared() {
        cancelTimers()
        fetchJob?.cancel()
        super.onCleared()
    }
}
