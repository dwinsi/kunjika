package com.keyfortress.app.core.backup

import android.util.Base64
import com.keyfortress.app.data.local.PasswordEntity
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupManager {
    private const val ITERATIONS = 65536
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    fun exportEncryptedBackup(passwords: List<PasswordEntity>, backupPassphrase: String): String {
        val rootJson = JSONObject()
        rootJson.put("version", 1)
        rootJson.put("exportedAt", System.currentTimeMillis())

        val array = JSONArray()
        for (item in passwords) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("username", item.username)
                put("encryptedPassword", item.encryptedPassword)
                put("websiteUrl", item.websiteUrl)
                put("category", item.category)
                put("notes", item.notes)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
                put("isFavorite", item.isFavorite)
                put("expiryDays", item.expiryDays)
            }
            array.put(obj)
        }
        rootJson.put("items", array)

        val plainJson = rootJson.toString()

        // Derive key from passphrase + random salt
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }

        val spec = PBEKeySpec(backupPassphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val cipherBytes = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))

        val exportPayload = JSONObject().apply {
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("data", Base64.encodeToString(cipherBytes, Base64.NO_WRAP))
        }

        return exportPayload.toString()
    }

    fun importEncryptedBackup(encryptedBackupJson: String, backupPassphrase: String): List<PasswordEntity> {
        val root = JSONObject(encryptedBackupJson)
        val salt = Base64.decode(root.getString("salt"), Base64.NO_WRAP)
        val iv = Base64.decode(root.getString("iv"), Base64.NO_WRAP)
        val cipherBytes = Base64.decode(root.getString("data"), Base64.NO_WRAP)

        val spec = PBEKeySpec(backupPassphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val decryptedBytes = cipher.doFinal(cipherBytes)
        val decryptedJson = String(decryptedBytes, Charsets.UTF_8)

        val dataRoot = JSONObject(decryptedJson)
        val itemsArray = dataRoot.getJSONArray("items")
        val list = mutableListOf<PasswordEntity>()

        for (i in 0 until itemsArray.length()) {
            val obj = itemsArray.getJSONObject(i)
            list.add(
                PasswordEntity(
                    id = if (obj.has("id")) obj.getLong("id") else 0L,
                    title = obj.getString("title"),
                    username = obj.optString("username", ""),
                    encryptedPassword = obj.getString("encryptedPassword"),
                    websiteUrl = obj.optString("websiteUrl", ""),
                    category = obj.optString("category", "Personal"),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    expiryDays = obj.optInt("expiryDays", 0)
                )
            )
        }

        return list
    }
}
