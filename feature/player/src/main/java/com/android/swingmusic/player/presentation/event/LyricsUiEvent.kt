package com.android.swingmusic.player.presentation.event

import com.android.swingmusic.core.domain.model.Track

sealed interface LyricsUiEvent {
    data class LoadLyrics(val track: Track) : LyricsUiEvent
    data class PositionChanged(val positionMs: Long) : LyricsUiEvent
    data class SetUserScrolled(val value: Boolean) : LyricsUiEvent
    data class SearchOnline(val track: Track) : LyricsUiEvent
}
