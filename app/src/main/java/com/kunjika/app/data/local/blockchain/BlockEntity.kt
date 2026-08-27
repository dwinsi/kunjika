package com.kunjika.app.data.local.blockchain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single block in the local immutable ledger.
 */
@Entity(tableName = "blockchain_ledger")
data class BlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val previousHash: String,    // Link to the previous block's hash
    val contentHash: String,     // Hash of the data (PasswordEntity)
    val action: String,          // CREATE, UPDATE, DELETE
    val timestamp: Long = System.currentTimeMillis(),
    val signature: String        // ECDSA signature of (previousHash + contentHash + action + timestamp)
)
