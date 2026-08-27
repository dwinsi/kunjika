package com.kunjika.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Manages a KeyStore key that is specifically tied to biometric authentication.
 * This key requires the user to authenticate before it can be used to initialize a Cipher.
 */
object BiometricKeyManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val BIOMETRIC_KEY_ALIAS = "KunjikaBiometricKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    init {
        initBiometricKey()
    }

    private fun initBiometricKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(BIOMETRIC_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                BIOMETRIC_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(true) // CRITICAL: This binds the key to biometrics
                .setInvalidatedByBiometricEnrollment(true) // Invalidate key if new fingerprint is added
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(BIOMETRIC_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            ?: throw IllegalStateException("Biometric key not found in AndroidKeyStore")
        return entry.secretKey
    }

    /**
     * Returns a Cipher initialized for encryption. 
     * This Cipher will be passed to BiometricPrompt.CryptoObject.
     */
    fun getEncryptionCipher(): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        return cipher
    }

    /**
     * In a real app, you would use this Cipher to actually encrypt/decrypt a small piece of data 
     * (like the Master PIN or a session token) after successful biometric auth.
     * For this implementation, we use the successful initialization of the Cipher as proof of hardware auth.
     */
    fun getDecryptionCipher(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher
    }
}
