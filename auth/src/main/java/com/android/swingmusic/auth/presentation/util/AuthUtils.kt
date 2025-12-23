package com.android.swingmusic.auth.presentation.util

object AuthUtils {
    fun normalizeUrl(url: String?): String? {
        val trimmed = url?.trim()
        if (trimmed.isNullOrEmpty()) return null
        // If it already has a scheme like http:// or https:// (or any scheme), leave it as-is
        val hasScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://").containsMatchIn(trimmed)
        return if (hasScheme) trimmed else "https://$trimmed"
    }

    /**
     * Validates URLs for login input.
     *
     * Rules:
     * - Allowed schemes: http, https, ftp
     * - Host: domain (with subdomains), localhost, or IPv4 address
     * - Optional port
     * - Optional path/query/fragment
     */
    fun validInputUrl(url: String?): Boolean {
        val urlRegex = Regex(
            pattern = "^(https?|ftp)://(" +
                    "localhost|" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}|" +
                    "(?:(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3})" +
                    ")(?::\\d{1,5})?(?:/\\S*)?$",
            options = setOf(RegexOption.IGNORE_CASE)
        )
        return url?.matches(urlRegex) == true
    }
}