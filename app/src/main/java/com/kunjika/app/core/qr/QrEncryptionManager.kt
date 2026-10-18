package com.kunjika.app.core.qr

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encrypts QR sync payloads using a temporary random 16-digit transfer code.
 */
object QrEncryptionManager {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 128
    private const val IV_SIZE = 12
    private const val SALT_SIZE = 16
    // The transfer code has 16 random decimal digits (~53 bits). This KDF cost
    // makes offline guessing expensive; use ECDH for future protocol revisions.
    private const val ITERATIONS = 600000
    private const val KEY_LENGTH = 256
    private const val SEPARATOR = "]"

    /**
     * Encrypts a payload string using a 16-digit random transfer code and random salt.
     */
    fun encrypt(payload: String, transferCode: String): String {
        require(transferCode.length == 16 && transferCode.all(Char::isDigit)) { "Transfer code must contain exactly 16 digits" }
        val salt = ByteArray(SALT_SIZE).apply { SecureRandom().nextBytes(this) }
        val key = deriveKey(transferCode, salt, ITERATIONS)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_SIZE).apply { SecureRandom().nextBytes(this) }
        
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
        
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val encryptedBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        
        return "$ivBase64$SEPARATOR$saltBase64$SEPARATOR$ITERATIONS$SEPARATOR$encryptedBase64"
    }

    /**
     * Decrypts an encrypted payload string using the transfer code.
     */
    fun decrypt(encryptedData: String, transferCode: String): String? {
        if (transferCode.length != 16 || !transferCode.all(Char::isDigit)) return null
        return try {
            val parts = encryptedData.split(SEPARATOR)
            if (parts.size != 4) return null
            val iterations = parts[2].toIntOrNull()?.takeIf { it == ITERATIONS } ?: return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val salt = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[3], Base64.NO_WRAP)
            if (iv.size != IV_SIZE || salt.size != SALT_SIZE || cipherText.size < TAG_LENGTH / 8) return null
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
        val passwordChars = transferCode.toCharArray()
        val spec = PBEKeySpec(passwordChars, salt, iterations, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return try {
            SecretKeySpec(factory.generateSecret(spec).encoded, ALGORITHM)
        } finally {
            spec.clearPassword()
            passwordChars.fill('\u0000')
        }
    }

    /**
     * Generates a random 16-digit transfer code.
     */
    fun generateTransferCode(): String {
        val random = SecureRandom()
        val code = StringBuilder()
        repeat(16) {
            code.append(random.nextInt(10))
        }
        return code.toString()
    }
}
