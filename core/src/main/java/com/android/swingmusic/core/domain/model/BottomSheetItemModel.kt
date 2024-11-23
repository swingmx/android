package com.android.swingmusic.core.domain.model

import com.android.swingmusic.core.domain.util.BottomSheetAction

data class BottomSheetItemModel(
    val enabled: Boolean = true,
    val label: String,
    val painterId: Int,
    val track: Track,
    val sheetAction: BottomSheetAction
)
