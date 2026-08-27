package com.kunjika.app.core.qr

import com.kunjika.app.data.repository.DecryptedPasswordItem
import org.junit.Assert.*
import org.junit.Test

class QrSyncPayloadTest {

    @Test
    fun `toJson and fromJson are consistent`() {
        val payload = QrSyncPayload(
            title = "Test",
            username = "user",
            password = "password123",
            websiteUrl = "https://test.com",
            category = "Work",
            notes = "Some notes",
            totpSecret = "JBSWY3DPEHPK3PXP"
        )
        
        val json = payload.toJson()
        val restored = QrSyncPayload.fromJson(json)
        
        assertEquals(payload, restored)
    }

    @Test
    fun `fromDecryptedItem maps correctly`() {
        val item = DecryptedPasswordItem(
            id = 1L,
            title = "Title",
            username = "User",
            plaintextPassword = "Pass",
            websiteUrl = "Url",
            category = "Cat",
            notes = "Notes",
            createdAt = 1000L,
            updatedAt = 2000L,
            isFavorite = true,
            expiryDays = 30,
            totpSecret = "Secret"
        )
        
        val payload = QrSyncPayload.fromDecryptedItem(item)
        
        assertEquals(item.title, payload.title)
        assertEquals(item.username, payload.username)
        assertEquals(item.plaintextPassword, payload.password)
        assertEquals(item.websiteUrl, payload.websiteUrl)
        assertEquals(item.category, payload.category)
        assertEquals(item.notes, payload.notes)
        assertEquals(item.totpSecret, payload.totpSecret)
    }
}
