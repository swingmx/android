package com.android.swingmusic.core.domain.model

enum class ChangeType { NEW, TWEAK, FIXED }

enum class ReleaseChannel { STABLE, BETA }

data class ChangeLink(val label: String, val url: String)

data class Change(
    val type: ChangeType,
    val text: String,
    val highlight: Boolean = false,
    val link: ChangeLink? = null,
)

data class Release(
    val versionName: String,
    val versionCode: Int,
    val date: String,
    val channel: ReleaseChannel,
    val changes: List<Change>,
) {
    val highlights: List<Change> get() = changes.filter { it.highlight }

    fun count(type: ChangeType): Int = changes.count { it.type == type }
}

/** [current] is the release of the running build, [earlier] the ones before it, newest first. */
data class Changelog(
    val current: Release,
    val earlier: List<Release>,
)
