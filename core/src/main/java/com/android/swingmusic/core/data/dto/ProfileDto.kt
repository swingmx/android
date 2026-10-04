package com.android.swingmusic.core.data.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class WeeklyStatsDto(
    @SerializedName("stats")
    val stats: List<StatItemDto>?
)

data class PairCodeDto(
    @SerializedName("code")
    val code: String?
)

data class ServerSettingsDto(
    @SerializedName("version")
    val version: String?
)

data class ChartResponseDto(
    @SerializedName("tracks")
    val tracks: List<JsonElement>?,
    @SerializedName("artists")
    val artists: List<JsonElement>?,
    @SerializedName("albums")
    val albums: List<JsonElement>?,
    @SerializedName("scrobbles")
    val scrobbles: ChartSummaryDto?
)

data class ChartSummaryDto(
    @SerializedName("text")
    val text: String?,
    @SerializedName("trend")
    val trend: String?
)

/** The fields /logger/top-* adds on top of the usual track, artist or album object. */
data class ChartMetaDto(
    @SerializedName("help_text")
    val helpText: String?,
    @SerializedName("trend")
    val trend: ChartTrendDto?
)

data class ChartTrendDto(
    @SerializedName("trend")
    val trend: String?,
    @SerializedName("is_new")
    val isNew: Boolean?
)

data class TriggerScanRequestDto(
    @SerializedName("full_scan")
    val fullScan: Boolean
)
