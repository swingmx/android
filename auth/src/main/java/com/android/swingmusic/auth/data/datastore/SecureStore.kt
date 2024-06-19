package com.android.swingmusic.auth.data.datastore

import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class SecureStore @Inject constructor(private val keyStore: KeyStore) {

    private val KEY_ALIAS = "auth_tokens_key"

    fun encrypt(data: String): String {
        val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry?
        val secretKey = secretKeyEntry?.secretKey ?: return "Encrypt: Secret Key NOT Found"

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray())

        val combinedBytes = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combinedBytes, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combinedBytes, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combinedBytes, Base64.DEFAULT)
    }

    fun decrypt(data: String): String {
        val encryptedBytes = Base64.decode(data, Base64.DEFAULT)

        val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry?
        val secretKey = secretKeyEntry?.secretKey ?: return "Decrypt: Secret Key NOT Found"

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(encryptedBytes, 0, 12))

        val decryptedBytes = cipher.doFinal(encryptedBytes, 12, encryptedBytes.size - 12)

        return String(decryptedBytes)
    }
}
