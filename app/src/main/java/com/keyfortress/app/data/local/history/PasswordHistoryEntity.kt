package com.keyfortress.app.data.local.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_history")
data class PasswordHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val encryptedPassword: String,
    val type: String, // PASSWORD, PASSPHRASE, PIN
    val timestamp: Long = System.currentTimeMillis()
)
