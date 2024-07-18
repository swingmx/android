package com.android.swingmusic.network.data.mapper

import com.android.swingmusic.network.data.dto.LogTrackRequestDto
import com.android.swingmusic.network.domain.model.LogTrackRequest

fun LogTrackRequest.toDto(): LogTrackRequestDto {
    return LogTrackRequestDto(
        duration = duration,
        source = source,
        timestamp = timestamp,
        trackHash = trackHash
    )
}
