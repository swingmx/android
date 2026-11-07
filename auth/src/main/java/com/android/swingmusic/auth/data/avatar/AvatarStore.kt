package com.android.swingmusic.auth.data.avatar

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
 * What to show for the signed-in user: [model] is an image Coil can load (a `file://` URI for
 * a photo saved on this device, or the server's URL), null means show [initials].
 */
data class Avatar(val model: String? = null, val initials: String = "")

/**
 * The signed-in user's profile photo. The server copy is the source of truth; a photo cropped
 * on this device is kept as a local file until it has been uploaded (or for good, if the
 * upload fails), and shown ahead of the server copy while it exists.
 */
@Singleton
class AvatarStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dir = File(context.filesDir, "avatars")
    private var ownerKey: String? = null
    private var localFile: File? = null
    private var serverImageUrl: String? = null
    private var serverImageHidden = false
    private var initials = ""

    private val _avatar = MutableStateFlow(Avatar())
    val avatar: StateFlow<Avatar> = _avatar.asStateFlow()

    /** Called by the auth layer whenever the signed-in user is known, or with nulls on logout. */
    suspend fun setOwner(
        baseUrl: String?,
        userId: Int?,
        serverImageUrl: String? = null,
        initials: String = "",
    ) = withContext(Dispatchers.IO) {
        val key = if (baseUrl.isNullOrBlank() || userId == null) null else keyFor(baseUrl, userId)
        if (key != ownerKey) {
            dir.listFiles { file -> file.name.endsWith(REMOVED_SUFFIX) }?.forEach { it.delete() }
            serverImageHidden = false
        }
        ownerKey = key
        this@AvatarStore.serverImageUrl = serverImageUrl?.takeIf { it.isNotBlank() }
        this@AvatarStore.initials = initials
        localFile = key?.let { latestFor(it) }
        publish()
    }

    /** The photo saved on this device that still needs uploading, if any. */
    fun localFile(): File? = localFile

    suspend fun save(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val key = ownerKey ?: return@withContext false
        runCatching {
            dir.mkdirs()
            val file = File(dir, "${key}_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            filesFor(key).filter { it != file }.forEach { it.delete() }
            localFile = file
            serverImageHidden = false
            publish()
        }.onFailure { Timber.tag("AVATAR").e(it) }.isSuccess
    }

    /** After a successful upload the server copy takes over. */
    suspend fun clearLocal() = withContext(Dispatchers.IO) {
        ownerKey?.let { key -> filesFor(key).forEach { it.delete() } }
        localFile = null
        publish()
    }

    /** Hides the photo but keeps the file until [discardRemoved], so it can be undone. */
    suspend fun remove() = withContext(Dispatchers.IO) {
        localFile?.let { it.renameTo(File(dir, it.name + REMOVED_SUFFIX)) }
        localFile = null
        serverImageHidden = true
        publish()
    }

    suspend fun undoRemove() = withContext(Dispatchers.IO) {
        val key = ownerKey ?: return@withContext
        val removed = dir.listFiles { file ->
            file.name.startsWith("${key}_") && file.name.endsWith(REMOVED_SUFFIX)
        }?.maxByOrNull { it.lastModified() }
        if (removed != null) {
            val restored = File(dir, removed.name.removeSuffix(REMOVED_SUFFIX))
            if (removed.renameTo(restored)) localFile = restored
        }
        serverImageHidden = false
        publish()
    }

    /** True while a removal is pending and the server still has a photo to delete. */
    fun hasHiddenServerImage(): Boolean = serverImageHidden && serverImageUrl != null

    suspend fun discardRemoved() = withContext(Dispatchers.IO) {
        val key = ownerKey ?: return@withContext
        dir.listFiles { file ->
            file.name.startsWith("${key}_") && file.name.endsWith(REMOVED_SUFFIX)
        }?.forEach { it.delete() }
    }

    /** Shows the server photo again, e.g. when deleting it on the server failed. */
    fun unhideServerImage() {
        serverImageHidden = false
        publish()
    }

    private fun publish() {
        _avatar.value = Avatar(
            model = localFile?.let { Uri.fromFile(it).toString() }
                ?: serverImageUrl?.takeUnless { serverImageHidden },
            initials = initials
        )
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
