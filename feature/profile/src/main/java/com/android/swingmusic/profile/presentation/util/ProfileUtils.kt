package com.android.swingmusic.profile.presentation.util

import android.content.Context
import java.util.Locale

internal fun Context.appVersion(): String {
    return runCatching { packageManager.getPackageInfo(packageName, 0).versionName }
        .getOrNull()
        .orEmpty()
}

internal fun formatBytes(bytes: Long): String {
    val value = bytes.coerceAtLeast(0L).toDouble()
    return when {
        value >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f GB", value / (1024 * 1024 * 1024))
        value >= 1024 * 1024 -> String.format(Locale.US, "%.0f MB", value / (1024 * 1024))
        value >= 1024 -> String.format(Locale.US, "%.0f KB", value / 1024)
        else -> "${value.toLong()} B"
    }
}
