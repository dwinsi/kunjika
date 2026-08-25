package com.keyfortress.app.core.qr

import com.google.gson.Gson
import com.keyfortress.app.data.repository.DecryptedPasswordItem

data class QrSyncPayload(
    val title: String,
    val username: String,
    val password: String,
    val websiteUrl: String,
    val category: String,
    val notes: String,
    val totpSecret: String? = null
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): QrSyncPayload? {
            return try {
                Gson().fromJson(json, QrSyncPayload::class.java)
            } catch (e: Exception) {
                null
            }
        }

        fun fromDecryptedItem(item: DecryptedPasswordItem): QrSyncPayload {
            return QrSyncPayload(
                title = item.title,
                username = item.username,
                password = item.plaintextPassword,
                websiteUrl = item.websiteUrl,
                category = item.category,
                notes = item.notes,
                totpSecret = item.totpSecret
            )
        }
    }
}
