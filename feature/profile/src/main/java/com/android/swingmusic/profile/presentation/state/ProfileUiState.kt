package com.android.swingmusic.profile.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.StatItem
import com.android.swingmusic.database.domain.model.User
import java.net.URI

@Immutable
internal data class ProfileUiState(
    val baseUrl: String = "",
    val user: User? = null,
    /** Local-only profile photo for this account. */
    val avatarPath: String? = null,
    val showPhotoViewer: Boolean = false,

    val isLoadingStats: Boolean = true,
    val errorLoadingStats: String? = null,
    val stats: List<StatItem> = emptyList(),

    val appVersion: String = "",
    val serverVersion: String? = null,

    val showLogOutDialog: Boolean = false,
    val isLoggingOut: Boolean = false,
)

internal val User.isAdmin: Boolean
    get() = roles.any { it.equals("admin", ignoreCase = true) }

/** For display only: "eric" → "Eric". Editing and login keep the stored username. */
internal fun displayName(username: String): String = username.replaceFirstChar { it.uppercaseChar() }

internal val User.displayName: String
    get() = displayName(username)

internal val User.initials: String
    get() {
        val fromNames = listOf(firstname, lastname)
            .mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }
            .joinToString("")
        return fromNames.ifEmpty { username.trim().take(1).uppercase() }
    }

/** "https://neon.example.com:1970/" -> "neon.example.com:1970" */
internal fun serverHost(baseUrl: String): String {
    val uri = runCatching { URI(baseUrl) }.getOrNull()
    val host = uri?.host ?: return baseUrl.substringAfter("://").trimEnd('/')
    return if (uri.port != -1) "$host:${uri.port}" else host
}
