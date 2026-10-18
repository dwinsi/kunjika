package com.kunjika.app.core.webdrop

import com.google.gson.Gson
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class WebDropQrPayload(
    val v: Int = 2,
    val sid: String,
    val pk: String,
    val svc: String,
    val t: Long
)

object WebDropCrypto {
    private val consumedSessionIds = LinkedHashSet<String>()

    @Synchronized
    fun consumeSessionId(sessionId: String): Boolean {
        if (!sessionId.matches(Regex("[0-9a-f]{32}")) || !consumedSessionIds.add(sessionId)) return false
        while (consumedSessionIds.size > 128) consumedSessionIds.remove(consumedSessionIds.first())
        return true
    }

    fun parseQrPayload(rawJson: String): WebDropQrPayload? {
        return try {
            Gson().fromJson(rawJson, WebDropQrPayload::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifies the timestamp is within the short pairing window.
     */
    fun verifyTimeWindow(timestamp: Long, maxDriftSeconds: Long = 120): Boolean {
        val now = System.currentTimeMillis() / 1000
        return Math.abs(now - timestamp) <= maxDriftSeconds
    }

    /**
     * Generates an ephemeral P-256 (secp256r1) keypair for the transfer session.
     */
    fun generateEphemeralKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        return kpg.generateKeyPair()
    }

    /**
     * Parses an uncompressed 65-byte P-256 public key from hex (starts with 04).
     */
    fun parseUncompressedP256PublicKey(hex: String): PublicKey {
        val raw = hexToBytes(hex)
        require(raw.size == 65 && raw[0] == 0x04.toByte()) { "Invalid uncompressed P-256 key" }

        val xBytes = raw.copyOfRange(1, 33)
        val yBytes = raw.copyOfRange(33, 65)
        val x = BigInteger(1, xBytes)
        val y = BigInteger(1, yBytes)
        val point = ECPoint(x, y)

        val kf = KeyFactory.getInstance("EC")
        val params = AlgorithmParameters.getInstance("EC").apply {
            init(ECGenParameterSpec("secp256r1"))
        }.getParameterSpec(ECParameterSpec::class.java)

        return kf.generatePublic(ECPublicKeySpec(point, params))
    }

    /**
     * Exports a P-256 public key as an uncompressed 65-byte hex string (04 + X + Y).
     */
    fun exportUncompressedPublicKey(publicKey: PublicKey): String {
        return bytesToHex(exportUncompressedPublicKeyBytes(publicKey))
    }

    /**
     * Exports a P-256 public key as an uncompressed 65-byte array (04 + X + Y).
     */
    fun exportUncompressedPublicKeyBytes(publicKey: PublicKey): ByteArray {
        val ecKey = publicKey as ECPublicKey
        val w = ecKey.w
        val xBytes = padOrTrimTo32(w.affineX.toByteArray())
        val yBytes = padOrTrimTo32(w.affineY.toByteArray())

        val out = ByteArray(65)
        out[0] = 0x04
        System.arraycopy(xBytes, 0, out, 1, 32)
        System.arraycopy(yBytes, 0, out, 33, 32)
        return out
    }

    /**
     * Derives a 256-bit shared secret via ECDH.
     */
    fun deriveSharedSecret(phonePrivateKey: PrivateKey, webPublicKey: PublicKey): ByteArray {
        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(phonePrivateKey)
        ka.doPhase(webPublicKey, true)
        return ka.generateSecret()
    }

    /** HKDF-SHA-256 session key, bound to both public keys and the one-time QR session. */
    fun deriveSessionKey(sharedSecret: ByteArray, sessionId: String, webPublicKey: ByteArray, phonePublicKey: ByteArray): ByteArray {
        val transcript = "kunjika-web-drop-v2|$sessionId|${bytesToHex(webPublicKey)}|${bytesToHex(phonePublicKey)}".toByteArray(Charsets.UTF_8)
        val salt = MessageDigest.getInstance("SHA-256").digest(transcript)
        return hkdfExpand(sharedSecret, salt, "kunjika-web-drop-v2/aes-gcm")
    }

    fun derivePairingCodeKey(sharedSecret: ByteArray, sessionId: String, webPublicKey: ByteArray, phonePublicKey: ByteArray): ByteArray {
        val transcript = "kunjika-web-drop-v2|$sessionId|${bytesToHex(webPublicKey)}|${bytesToHex(phonePublicKey)}".toByteArray(Charsets.UTF_8)
        val salt = MessageDigest.getInstance("SHA-256").digest(transcript)
        return hkdfExpand(sharedSecret, salt, "kunjika-web-drop-v2/pairing-code")
    }

    private fun hkdfExpand(sharedSecret: ByteArray, salt: ByteArray, info: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = mac.doFinal(sharedSecret)
        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        return mac.doFinal("$info\u0001".toByteArray(Charsets.UTF_8))
    }

    /**
     * Computes a six-digit short authentication string for transcript comparison.
     */
    fun computePairingCode(sessionKey: ByteArray): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(sessionKey, "HmacSHA256"))
        val hmac = mac.doFinal("kunjika-web-drop-v2/pairing-code".toByteArray(Charsets.UTF_8))

        val offset = hmac[hmac.size - 1].toInt() and 0x0f
        val binary = ((hmac[offset].toInt() and 0x7f) shl 24) or
                ((hmac[offset + 1].toInt() and 0xff) shl 16) or
                ((hmac[offset + 2].toInt() and 0xff) shl 8) or
                (hmac[offset + 3].toInt() and 0xff)

        val otp = binary % 1000000
        return String.format("%06d", otp)
    }

    /**
     * Encrypts a JSON payload using AES-256-GCM.
     * Format: IV (12 bytes) + CipherText (with 16-byte auth tag).
     */
    fun encryptPayload(jsonPayload: String, sessionKey: ByteArray): ByteArray {
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        val keySpec = SecretKeySpec(sessionKey, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(jsonPayload.toByteArray(Charsets.UTF_8))

        val combined = ByteArray(12 + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, 12)
        System.arraycopy(cipherText, 0, combined, 12, cipherText.size)
        return combined
    }

    // Helper functions
    private fun padOrTrimTo32(bytes: ByteArray): ByteArray {
        if (bytes.size == 32) return bytes
        if (bytes.size > 32) {
            // Trim leading zero from BigInteger positive sign byte
            return bytes.copyOfRange(bytes.size - 32, bytes.size)
        }
        val padded = ByteArray(32)
        System.arraycopy(bytes, 0, padded, 32 - bytes.size, bytes.size)
        return padded
    }

    fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
