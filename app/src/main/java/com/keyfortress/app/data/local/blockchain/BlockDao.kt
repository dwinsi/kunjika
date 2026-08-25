package com.keyfortress.app.data.local.blockchain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Query("SELECT * FROM blockchain_ledger ORDER BY id ASC")
    fun getAllBlocks(): Flow<List<BlockEntity>>

    @Query("SELECT * FROM blockchain_ledger ORDER BY id DESC LIMIT 1")
    suspend fun getLatestBlock(): BlockEntity?

    @Insert
    suspend fun insertBlock(block: BlockEntity)

    @Query("DELETE FROM blockchain_ledger")
    suspend fun clearLedger()

    @Query("SELECT COUNT(*) FROM blockchain_ledger")
    suspend fun getBlockCount(): Int
}
