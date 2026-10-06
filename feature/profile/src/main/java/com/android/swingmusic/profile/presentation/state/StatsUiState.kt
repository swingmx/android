package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Chart
import com.android.swingmusic.core.domain.model.Track

internal enum class StatsPeriod(val apiValue: String, val label: String) {
    WEEK("week", "Week"),
    MONTH("month", "Month"),
    YEAR("year", "Year"),
    ALL_TIME("alltime", "All time")
}

internal enum class StatsOrder(val apiValue: String, val label: String) {
    PLAY_TIME("playduration", "By time"),
    PLAY_COUNT("playcount", "By plays")
}

/** Each chart loads on its own: the server takes several seconds per chart. */
internal sealed interface ChartState<out T> {
    data object Loading : ChartState<Nothing>
    data class Error(val message: String) : ChartState<Nothing>
    data class Loaded<T>(val chart: Chart<T>) : ChartState<T>
}

@Immutable
internal data class StatsUiState(
    val baseUrl: String = "",
    val period: StatsPeriod = StatsPeriod.WEEK,
    val order: StatsOrder = StatsOrder.PLAY_TIME,
    val isRefreshing: Boolean = false,

    val tracks: ChartState<Track> = ChartState.Loading,
    val artists: ChartState<Artist> = ChartState.Loading,
    val albums: ChartState<Album> = ChartState.Loading,
)

internal val <T> ChartState<T>.chart: Chart<T>?
    get() = (this as? ChartState.Loaded)?.chart

internal val StatsUiState.isEmpty: Boolean
    get() = listOf(tracks, artists, albums).all { it is ChartState.Loaded && it.chart.entries.isEmpty() }

internal val StatsUiState.allFailed: Boolean
    get() = listOf(tracks, artists, albums).all { it is ChartState.Error }
