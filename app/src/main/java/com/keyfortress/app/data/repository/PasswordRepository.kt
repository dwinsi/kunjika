package com.keyfortress.app.data.repository

import com.keyfortress.app.core.security.KeystoreManager
import com.keyfortress.app.data.local.PasswordDao
import com.keyfortress.app.data.local.PasswordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DecryptedPasswordItem(
    val id: Long,
    val title: String,
    val username: String,
    val plaintextPassword: String,
    val websiteUrl: String,
    val category: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean
)

class PasswordRepository(private val passwordDao: PasswordDao) {

    fun getAllPasswords(): Flow<List<DecryptedPasswordItem>> {
        return passwordDao.getAllPasswords().map { list ->
            list.map { entity -> entity.toDecrypted() }
        }
    }

    fun getPasswordsByCategory(category: String): Flow<List<DecryptedPasswordItem>> {
        return if (category == "All") {
            getAllPasswords()
        } else {
            passwordDao.getPasswordsByCategory(category).map { list ->
                list.map { entity -> entity.toDecrypted() }
            }
        }
    }

    fun searchPasswords(query: String): Flow<List<DecryptedPasswordItem>> {
        return passwordDao.searchPasswords(query).map { list ->
            list.map { entity -> entity.toDecrypted() }
        }
    }

    suspend fun getPasswordById(id: Long): DecryptedPasswordItem? {
        return passwordDao.getPasswordById(id)?.toDecrypted()
    }

    suspend fun getPasswordsByDomain(domain: String): List<DecryptedPasswordItem> {
        return passwordDao.getPasswordsByDomain(domain).map { it.toDecrypted() }
    }

    suspend fun savePassword(
        id: Long = 0L,
        title: String,
        username: String,
        plainPassword: String,
        websiteUrl: String,
        category: String,
        notes: String,
        isFavorite: Boolean = false
    ): Long {
        val encryptedPassword = KeystoreManager.encrypt(plainPassword)
        val entity = PasswordEntity(
            id = id,
            title = title,
            username = username,
            encryptedPassword = encryptedPassword,
            websiteUrl = websiteUrl,
            category = category,
            notes = notes,
            createdAt = if (id == 0L) System.currentTimeMillis() else (passwordDao.getPasswordById(id)?.createdAt ?: System.currentTimeMillis()),
            updatedAt = System.currentTimeMillis(),
            isFavorite = isFavorite
        )
        return passwordDao.insertPassword(entity)
    }

    suspend fun toggleFavorite(item: DecryptedPasswordItem) {
        val entity = passwordDao.getPasswordById(item.id) ?: return
        passwordDao.updatePassword(entity.copy(isFavorite = !entity.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePassword(id: Long) {
        passwordDao.deletePasswordById(id)
    }

    suspend fun getRawEntities(): List<PasswordEntity> {
        return passwordDao.getAllPasswordsSync()
    }

    suspend fun importRawEntities(entities: List<PasswordEntity>) {
        passwordDao.insertPasswords(entities)
    }

    private fun PasswordEntity.toDecrypted(): DecryptedPasswordItem {
        return DecryptedPasswordItem(
            id = id,
            title = title,
            username = username,
            plaintextPassword = KeystoreManager.decrypt(encryptedPassword),
            websiteUrl = websiteUrl,
            category = category,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isFavorite = isFavorite
        )
    }
}
