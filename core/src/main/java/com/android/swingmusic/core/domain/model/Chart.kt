package com.android.swingmusic.core.domain.model

enum class Trend { RISING, FALLING, STABLE }

data class ChartEntry<T>(
    val item: T,
    val helpText: String,
    val trend: Trend,
    val isNew: Boolean
)

/** Top tracks, artists or albums for a period, from /logger/top-*. */
data class Chart<T>(
    val entries: List<ChartEntry<T>>,
    val summary: String,
    val summaryTrend: Trend
)
