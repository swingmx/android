package com.android.swingmusic.player.presentation.state

import com.android.swingmusic.core.domain.model.LyricsLine

data class LyricsUiState(
    val lines: List<LyricsLine> = emptyList(),
    val synced: Boolean = true,
    val exists: Boolean = false,
    val copyright: String = "",
    val currentLine: Int = -1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pluginSearching: Boolean = false,
    val pluginError: String? = null,
    val userScrolled: Boolean = false,
    val trackHash: String = ""
)
