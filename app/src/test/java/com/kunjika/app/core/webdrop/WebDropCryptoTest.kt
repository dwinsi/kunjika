package com.kunjika.app.core.webdrop

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class WebDropCryptoTest {

    @Test
    fun `session ids are one use within the running process`() {
        val sessionId = "1234567890abcdef1234567890abcdef"
        assertTrue(WebDropCrypto.consumeSessionId(sessionId))
        assertFalse(WebDropCrypto.consumeSessionId(sessionId))
    }

    @Test
    fun `time window verification catches stale sessions`() {
        val now = System.currentTimeMillis() / 1000
        assertTrue(WebDropCrypto.verifyTimeWindow(now, maxDriftSeconds = 60))
        assertTrue(WebDropCrypto.verifyTimeWindow(now - 30, maxDriftSeconds = 60))
        assertFalse(WebDropCrypto.verifyTimeWindow(now - 120, maxDriftSeconds = 60))
    }

    @Test
    fun `ecdh key agreement between web and phone derives identical shared secrets`() {
        // 1. Simulate Web Companion KeyPair
        val webKeyPair = WebDropCrypto.generateEphemeralKeyPair()
        val webPubKeyHex = WebDropCrypto.exportUncompressedPublicKey(webKeyPair.public)
        assertTrue("Web public key should start with 04", webPubKeyHex.startsWith("04"))
        assertEquals("Uncompressed P-256 key is 65 bytes = 130 hex chars", 130, webPubKeyHex.length)

        // 2. Simulate Phone parsing Web's public key
        val parsedWebPubKey = WebDropCrypto.parseUncompressedP256PublicKey(webPubKeyHex)
        assertNotNull(parsedWebPubKey)

        // 3. Phone generates its own KeyPair
        val phoneKeyPair = WebDropCrypto.generateEphemeralKeyPair()

        // 4. Phone derives shared secret
        val phoneSharedSecret = WebDropCrypto.deriveSharedSecret(phoneKeyPair.private, parsedWebPubKey)

        // 5. Web derives shared secret using Phone's public key
        val webKa = KeyAgreement.getInstance("ECDH")
        webKa.init(webKeyPair.private)
        webKa.doPhase(phoneKeyPair.public, true)
        val webSharedSecret = webKa.generateSecret()

        // 6. Assert both derived secrets are 100% identical!
        assertArrayEquals(webSharedSecret, phoneSharedSecret)
        assertEquals(32, phoneSharedSecret.size)
    }

    @Test
    fun `transcript-derived pairing codes are stable six digit values`() {
        val sharedSecret = ByteArray(32) { (it + 1).toByte() }
        val browserPublic = ByteArray(65) { (it + 1).toByte() }
        val phonePublic = ByteArray(65) { (it + 2).toByte() }
        val codeKey = WebDropCrypto.derivePairingCodeKey(sharedSecret, "test-session", browserPublic, phonePublic)
        val code1 = WebDropCrypto.computePairingCode(codeKey)
        val code2 = WebDropCrypto.computePairingCode(codeKey)

        assertEquals(code1, code2)
        assertEquals(6, code1.length)
        assertTrue(code1.all { it.isDigit() })
    }

    @Test
    fun `aes gcm payload encryption uses derived session key`() {
        val sessionKey = ByteArray(32) { 42.toByte() }
        val payload = """{"title":"GitHub","password":"SecretPassword123!"}"""

        val encryptedBytes = WebDropCrypto.encryptPayload(payload, sessionKey)
        assertTrue(encryptedBytes.size > 28) // 12-byte IV + ciphertext + 16-byte auth tag

        // Decrypt
        val iv = encryptedBytes.copyOfRange(0, 12)
        val cipherText = encryptedBytes.copyOfRange(12, encryptedBytes.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(sessionKey, "AES"), GCMParameterSpec(128, iv))
        val decryptedBytes = cipher.doFinal(cipherText)
        val decryptedString = String(decryptedBytes, Charsets.UTF_8)

        assertEquals(payload, decryptedString)
    }
}
