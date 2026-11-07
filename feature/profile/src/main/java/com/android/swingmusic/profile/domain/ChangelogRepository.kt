package com.android.swingmusic.profile.domain

import com.android.swingmusic.core.domain.model.Changelog

interface ChangelogRepository {
    /** Null when the bundled changelog is missing, broken or has no entry for this build. */
    suspend fun getChangelog(): Changelog?

    /** True until the running build's release notes have been dismissed once. */
    suspend fun isUnseen(): Boolean

    /** A first install (as opposed to an update) that has never dismissed the notes. */
    suspend fun isFirstLaunch(): Boolean

    suspend fun markSeen()
}
