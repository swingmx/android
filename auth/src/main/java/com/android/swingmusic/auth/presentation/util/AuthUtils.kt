package com.android.swingmusic.auth.presentation.util

object AuthUtils {
    fun normalizeUrl(url: String?): String? {
        val trimmed = url?.trim()
        if (trimmed.isNullOrEmpty()) return null
        // If it already has a scheme like http:// or https:// (or any scheme), leave it as-is
        val hasScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://").containsMatchIn(trimmed)
        return if (hasScheme) trimmed else "https://$trimmed"
    }

    fun validInputUrl(url: String?): Boolean {
        val urlRegex = Regex("^(https?|ftp)://[^\\s/$.?#].\\S*$")
        return url?.matches(urlRegex) == true
    }
}