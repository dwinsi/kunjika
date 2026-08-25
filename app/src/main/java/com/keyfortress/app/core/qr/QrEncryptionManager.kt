package com.keyfortress.app.core.qr

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages encryption and decryption of QR sync payloads using a temporary 6-digit transfer code.
 */
object QrEncryptionManager {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 128
    private const val IV_SIZE = 12
    private const val SALT_SIZE = 16
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SEPARATOR = "]"

    // Static salt for QR sync - it's public knowledge, security comes from the transfer code
    private val STATIC_SALT = "KeyFortressQrSyncSalt".toByteArray(Charsets.UTF_8)

    /**
     * Encrypts a payload string using a 6-digit transfer code.
     */
    fun encrypt(payload: String, transferCode: String): String {
        val key = deriveKey(transferCode)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_SIZE).apply { SecureRandom().nextBytes(this) }
        
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
        
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encryptedBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        
        return "$ivBase64$SEPARATOR$encryptedBase64"
    }

    /**
     * Decrypts an encrypted payload string using the 6-digit transfer code.
     */
    fun decrypt(encryptedData: String, transferCode: String): String? {
        return try {
            val parts = encryptedData.split(SEPARATOR)
            if (parts.size != 2) return null
            
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
            
            val key = deriveKey(transferCode)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
            
            val decrypted = cipher.doFinal(cipherText)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private fun deriveKey(transferCode: String): SecretKeySpec {
        val spec = PBEKeySpec(transferCode.toCharArray(), STATIC_SALT, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    /**
     * Generates a random 6-digit transfer code.
     */
    fun generateTransferCode(): String {
        val random = SecureRandom()
        val code = StringBuilder()
        repeat(6) {
            code.append(random.nextInt(10))
        }
        return code.toString()
    }
}
