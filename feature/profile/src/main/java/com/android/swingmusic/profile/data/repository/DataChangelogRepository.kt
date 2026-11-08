package com.android.swingmusic.profile.data.repository

import android.content.Context
import android.os.Build
import com.android.swingmusic.core.domain.model.Change
import com.android.swingmusic.core.domain.model.ChangeLink
import com.android.swingmusic.core.domain.model.ChangeType
import com.android.swingmusic.core.domain.model.Changelog
import com.android.swingmusic.core.domain.model.Release
import com.android.swingmusic.core.domain.model.ReleaseChannel
import com.android.swingmusic.profile.domain.ChangelogRepository
import com.android.swingmusic.settings.domain.repository.AppSettingsRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataChangelogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: AppSettingsRepository,
) : ChangelogRepository {

    private val runningVersionCode: Int by lazy {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toInt()
            else @Suppress("DEPRECATION") info.versionCode
        }.getOrDefault(0)
    }

    private var cached: Changelog? = null

    override suspend fun getChangelog(): Changelog? = cached ?: withContext(Dispatchers.IO) {
        runCatching {
            val json = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
            Gson().fromJson(json, ChangelogDto::class.java).toChangelog(runningVersionCode)
        }.onFailure { Timber.tag("CHANGELOG").e(it, "Couldn't read $ASSET_NAME") }
            .getOrNull()
            ?.also { cached = it }
    }

    override suspend fun isUnseen(): Boolean {
        val current = getChangelog()?.current ?: return false
        return settings.lastSeenChangelogVersionCode.first() < current.versionCode
    }

    override suspend fun isFirstLaunch(): Boolean {
        if (settings.lastSeenChangelogVersionCode.first() != 0) return false
        // Updating from a build without release notes is not a first install.
        return runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.firstInstallTime == info.lastUpdateTime
        }.getOrDefault(true)
    }

    override suspend fun markSeen() {
        val current = getChangelog()?.current ?: return
        settings.setLastSeenChangelogVersionCode(current.versionCode)
    }

    private companion object {
        const val ASSET_NAME = "changelog.json"
    }
}

private class ChangelogDto(val releases: List<ReleaseDto>?)

private class ReleaseDto(
    @SerializedName("versionName") val versionName: String?,
    @SerializedName("versionCode") val versionCode: Int?,
    val date: String?,
    val channel: String?,
    val changes: List<ChangeDto>?,
)

private class ChangeDto(
    val type: String?,
    val text: String?,
    val highlight: Boolean?,
    val link: LinkDto?,
)

private class LinkDto(val label: String?, val url: String?)

private fun ChangelogDto.toChangelog(runningVersionCode: Int): Changelog? {
    val releases = releases.orEmpty()
        .mapNotNull { it.toRelease() }
        .filter { it.versionCode <= runningVersionCode }
        .sortedByDescending { it.versionCode }
    val current = releases.firstOrNull() ?: return null
    return Changelog(current = current, earlier = releases.drop(1))
}

private fun ReleaseDto.toRelease(): Release? {
    val name = versionName ?: return null
    val code = versionCode ?: return null
    val changes = changes.orEmpty().mapNotNull { it.toChange() }
    if (changes.isEmpty()) return null
    return Release(
        versionName = name,
        versionCode = code,
        date = date.orEmpty(),
        channel = if (channel.equals("beta", ignoreCase = true)) ReleaseChannel.BETA else ReleaseChannel.STABLE,
        changes = changes.sortedByDescending { it.highlight },
    )
}

private fun ChangeDto.toChange(): Change? {
    val text = text?.takeIf { it.isNotBlank() } ?: return null
    val type = when (type?.lowercase()) {
        "new" -> ChangeType.NEW
        "tweak" -> ChangeType.TWEAK
        "fixed", "fix" -> ChangeType.FIXED
        else -> return null
    }
    val link = link?.let { l ->
        val url = l.url?.takeIf { it.isNotBlank() } ?: return@let null
        ChangeLink(label = l.label?.takeIf { it.isNotBlank() } ?: "Details", url = url)
    }
    return Change(type = type, text = text, highlight = highlight == true, link = link)
}
