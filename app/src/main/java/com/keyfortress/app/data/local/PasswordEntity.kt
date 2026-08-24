package com.keyfortress.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val username: String = "",
    val encryptedPassword: String,
    val websiteUrl: String = "",
    val category: String = "Personal",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val expiryDays: Int = 0 // 0 means no expiry
)
