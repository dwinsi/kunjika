package com.keyfortress.app.data.repository

import com.keyfortress.app.core.security.KeystoreManager
import com.keyfortress.app.data.local.history.PasswordHistoryDao
import com.keyfortress.app.data.local.history.PasswordHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class DecryptedHistoryItem(
    val id: Long,
    val plaintextPassword: String,
    val type: String,
    val timestamp: Long
)

class HistoryRepository(private val historyDao: PasswordHistoryDao) {

    val recentHistory: Flow<List<DecryptedHistoryItem>> = historyDao.getRecentHistory()
        .map { list ->
            list.map { entity ->
                DecryptedHistoryItem(
                    id = entity.id,
                    plaintextPassword = KeystoreManager.decrypt(entity.encryptedPassword),
                    type = entity.type,
                    timestamp = entity.timestamp
                )
            }
        }
        .flowOn(Dispatchers.IO)

    suspend fun addHistory(password: String, type: String) = withContext(Dispatchers.IO) {
        val encrypted = KeystoreManager.encrypt(password)
        historyDao.insertHistory(
            PasswordHistoryEntity(
                encryptedPassword = encrypted,
                type = type
            )
        )
        historyDao.pruneHistory()
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }
}
