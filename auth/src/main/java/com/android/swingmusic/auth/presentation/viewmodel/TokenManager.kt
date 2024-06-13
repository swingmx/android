package com.android.swingmusic.auth.presentation.viewmodel

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class TokenManagerViewModel @Inject constructor(private val keyStore: KeyStore) : ViewModel() {

    private val accessTokenName = "access_token"
    private val refreshTokenName = "refresh_token"

    init {
        // Initialize keyStore if not already done
        if (!keyStore.containsAlias(accessTokenName)) {
            generateKeyPair()
        }
    }

    private fun generateKeyPair() {
        viewModelScope.launch {
            val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
                accessTokenName, // alias
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).run {
                setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                build()
            }

            keyPairGenerator.initialize(parameterSpec)

            val keyPair = keyPairGenerator.generateKeyPair()
        }
    }

    fun storeAccessToken(accessToken: String) {
        val encryptedToken = encrypt(accessToken.toByteArray())
        val secretEntry = KeyStore.SecretKeyEntry(SecretKeySpec(encryptedToken, "AES"))
        keyStore.setEntry(accessTokenName, secretEntry, null)
    }


    fun storeRefreshToken(refreshToken: String) {
        val encryptedToken = encrypt(refreshToken.toByteArray())
        val secretEntry = KeyStore.SecretKeyEntry(SecretKeySpec(encryptedToken, "AES"))
        keyStore.setEntry(refreshTokenName, secretEntry, null)
    }

    fun getAccessToken(): String? {
        val privateKeyEntry = keyStore.getEntry(accessTokenName, null) as KeyStore.SecretKeyEntry
        return decrypt(privateKeyEntry.secretKey.encoded)
    }

    fun getRefreshToken(): String? {
        val privateKeyEntry = keyStore.getEntry(refreshTokenName, null) as KeyStore.SecretKeyEntry
        return decrypt(privateKeyEntry.secretKey.encoded)
    }

    private fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(accessTokenName, null))
        return cipher.doFinal(data)
    }

    private fun decrypt(data: ByteArray): String? {
        if (data.isEmpty()) return null
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, keyStore.getKey(refreshTokenName, null))
        return String(cipher.doFinal(data))
    }
}
