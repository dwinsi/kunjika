package com.kunjika.app.data.local.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordHistoryDao {
    @Query("SELECT * FROM password_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<PasswordHistoryEntity>>

    @Insert
    suspend fun insertHistory(history: PasswordHistoryEntity)

    @Query("DELETE FROM password_history")
    suspend fun clearHistory()

    @Query("DELETE FROM password_history WHERE id NOT IN (SELECT id FROM password_history ORDER BY timestamp DESC LIMIT 50)")
    suspend fun pruneHistory()
}
