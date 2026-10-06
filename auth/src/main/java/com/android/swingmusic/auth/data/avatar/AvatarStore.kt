package com.android.swingmusic.auth.data.avatar

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-only profile photos, one per server + user. Files outlive logout, so logging back
 * in to the same account brings the photo back. Every save gets a new file name, which keeps
 * image caches from showing the previous photo.
 */
@Singleton
class AvatarStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dir = File(context.filesDir, "avatars")
    private var ownerKey: String? = null

    private val _avatar = MutableStateFlow<File?>(null)

    /** The current account's photo, or null for initials. */
    val avatar: StateFlow<File?> = _avatar.asStateFlow()

    /** Called by the auth layer whenever the signed-in user is known, or with nulls on logout. */
    suspend fun setOwner(baseUrl: String?, userId: Int?) = withContext(Dispatchers.IO) {
        ownerKey = if (baseUrl.isNullOrBlank() || userId == null) null else keyFor(baseUrl, userId)
        dir.listFiles { file -> file.name.endsWith(REMOVED_SUFFIX) }?.forEach { it.delete() }
        _avatar.value = ownerKey?.let { latestFor(it) }
    }

    suspend fun save(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val key = ownerKey ?: return@withContext false
        runCatching {
            dir.mkdirs()
            val file = File(dir, "${key}_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            filesFor(key).filter { it != file }.forEach { it.delete() }
            _avatar.value = file
        }.onFailure { Timber.tag("AVATAR").e(it) }.isSuccess
    }

    /** Hides the photo but keeps the file until [discardRemoved], so it can be undone. */
    suspend fun remove() = withContext(Dispatchers.IO) {
        val current = _avatar.value ?: return@withContext
        current.renameTo(File(dir, current.name + REMOVED_SUFFIX))
        _avatar.value = null
    }

    suspend fun undoRemove() = withContext(Dispatchers.IO) {
        val key = ownerKey ?: return@withContext
        val removed = dir.listFiles { file ->
            file.name.startsWith("${key}_") && file.name.endsWith(REMOVED_SUFFIX)
        }?.maxByOrNull { it.lastModified() } ?: return@withContext
        val restored = File(dir, removed.name.removeSuffix(REMOVED_SUFFIX))
        if (removed.renameTo(restored)) _avatar.value = restored
    }

    suspend fun discardRemoved() = withContext(Dispatchers.IO) {
        val key = ownerKey ?: return@withContext
        dir.listFiles { file ->
            file.name.startsWith("${key}_") && file.name.endsWith(REMOVED_SUFFIX)
        }?.forEach { it.delete() }
    }

    private fun filesFor(key: String): List<File> =
        dir.listFiles { file -> file.name.startsWith("${key}_") && file.name.endsWith(".jpg") }
            ?.toList()
            .orEmpty()

    private fun latestFor(key: String): File? = filesFor(key).maxByOrNull { it.lastModified() }

    private fun keyFor(baseUrl: String, userId: Int): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(baseUrl.trimEnd('/').toByteArray())
        val server = digest.take(6).joinToString("") { "%02x".format(it) }
        return "${server}_$userId"
    }

    private companion object {
        const val REMOVED_SUFFIX = ".removed"
    }
}
