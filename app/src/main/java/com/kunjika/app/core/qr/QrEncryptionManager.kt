package com.kunjika.app.core.qr

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages hardened encryption and decryption of QR sync payloads using a temporary 6-digit transfer code.
 */
object QrEncryptionManager {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 128
    private const val IV_SIZE = 12
    private const val SALT_SIZE = 16
    private const val ITERATIONS = 100000
    private const val KEY_LENGTH = 256
    private const val SEPARATOR = "]"

    // Legacy static salt fallback
    private val LEGACY_STATIC_SALT = "KunjikaQrSyncSalt".toByteArray(Charsets.UTF_8)

    /**
     * Encrypts a payload string using a 6-digit transfer code and dynamic random salt.
     */
    fun encrypt(payload: String, transferCode: String): String {
        val salt = ByteArray(SALT_SIZE).apply { SecureRandom().nextBytes(this) }
        val key = deriveKey(transferCode, salt, ITERATIONS)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_SIZE).apply { SecureRandom().nextBytes(this) }
        
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
        
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val encryptedBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        
        return "$ivBase64$SEPARATOR$saltBase64$SEPARATOR$encryptedBase64"
    }

    /**
     * Decrypts an encrypted payload string using the 6-digit transfer code.
     */
    fun decrypt(encryptedData: String, transferCode: String): String? {
        return try {
            val parts = encryptedData.split(SEPARATOR)
            val (iv, salt, cipherText) = when (parts.size) {
                3 -> Triple(
                    Base64.decode(parts[0], Base64.NO_WRAP),
                    Base64.decode(parts[1], Base64.NO_WRAP),
                    Base64.decode(parts[2], Base64.NO_WRAP)
                )
                2 -> Triple(
                    Base64.decode(parts[0], Base64.NO_WRAP),
                    LEGACY_STATIC_SALT,
                    Base64.decode(parts[1], Base64.NO_WRAP)
                )
                else -> return null
            }
            
            val iterations = if (parts.size == 3) ITERATIONS else 10000
            val key = deriveKey(transferCode, salt, iterations)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
            
            val decrypted = cipher.doFinal(cipherText)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private fun deriveKey(transferCode: String, salt: ByteArray, iterations: Int = ITERATIONS): SecretKeySpec {
        val spec = PBEKeySpec(transferCode.toCharArray(), salt, iterations, KEY_LENGTH)
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
