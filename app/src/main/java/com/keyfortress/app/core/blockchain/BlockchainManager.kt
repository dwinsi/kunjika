package com.keyfortress.app.core.blockchain

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.keyfortress.app.data.local.blockchain.BlockEntity
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

/**
 * Manages the local blockchain logic, including hashing, signing, and verification.
 */
object BlockchainManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "KeyFortressIdentityKey"
    private const val SIGN_ALGORITHM = "SHA256withECDSA"

    init {
        initIdentityKey()
    }

    private fun initIdentityKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false) // Audit log should be non-interactive
                .build()

            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair()
        }
    }

    /**
     * Computes the SHA-256 hash of a string.
     */
    fun computeHash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /**
     * Signs the block data using the hardware-backed private key.
     */
    fun signBlock(previousHash: String, contentHash: String, action: String, timestamp: Long): String {
        val dataToSign = "$previousHash|$contentHash|$action|$timestamp"
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val privateKey = (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry).privateKey

        return Signature.getInstance(SIGN_ALGORITHM).run {
            initSign(privateKey)
            update(dataToSign.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(sign(), Base64.NO_WRAP)
        }
    }

    /**
     * Verifies the signature of a block.
     */
    fun verifyBlock(block: BlockEntity): Boolean {
        val dataToVerify = "${block.previousHash}|${block.contentHash}|${block.action}|${block.timestamp}"
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey

        return try {
            Signature.getInstance(SIGN_ALGORITHM).run {
                initVerify(publicKey)
                update(dataToVerify.toByteArray(Charsets.UTF_8))
                verify(Base64.decode(block.signature, Base64.NO_WRAP))
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifies the entire chain integrity.
     */
    fun verifyChain(blocks: List<BlockEntity>): Boolean {
        if (blocks.isEmpty()) return true

        var lastHash = "0" // Genesis previous hash
        for (block in blocks) {
            // 1. Verify Merkle Link
            if (block.previousHash != lastHash) return false
            
            // 2. Verify Signature
            if (!verifyBlock(block)) return false
            
            // Compute this block's hash for the next iteration
            // The block hash itself is a hash of its signed components
            lastHash = computeHash("${block.previousHash}|${block.contentHash}|${block.action}|${block.timestamp}|${block.signature}")
        }
        return true
    }
}
