package com.kunjika.app.data.repository

import com.kunjika.app.core.blockchain.BlockchainManager
import com.kunjika.app.core.security.KeystoreManager
import com.kunjika.app.core.util.KLog
import com.kunjika.app.data.local.PasswordDao
import com.kunjika.app.data.local.PasswordEntity
import com.kunjika.app.data.local.blockchain.BlockDao
import com.kunjika.app.data.local.blockchain.BlockEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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
    val isFavorite: Boolean,
    val expiryDays: Int,
    val totpSecret: String? = null
)

class PasswordRepository(
    private val passwordDao: PasswordDao,
    private val blockDao: BlockDao
) {

    fun getAllBlocks(): Flow<List<BlockEntity>> = blockDao.getAllBlocks()

    suspend fun verifyIntegrity(): Boolean = withContext(Dispatchers.IO) {
        val blocks = blockDao.getAllBlocks().first()
        BlockchainManager.verifyChain(blocks)
    }

    private suspend fun recordBlock(action: String, entity: PasswordEntity) {
        val latestBlock = blockDao.getLatestBlock()
        val previousHash = latestBlock?.let { 
            BlockchainManager.computeHash("${it.previousHash}|${it.contentHash}|${it.action}|${it.timestamp}|${it.signature}")
        } ?: "0"

        val contentHash = BlockchainManager.computeHash(entity.toString())
        val timestamp = System.currentTimeMillis()
        val signature = BlockchainManager.signBlock(previousHash, contentHash, action, timestamp)

        val block = BlockEntity(
            previousHash = previousHash,
            contentHash = contentHash,
            action = action,
            timestamp = timestamp,
            signature = signature
        )
        blockDao.insertBlock(block)
    }

    suspend fun recordExportBlock(action: String, itemId: Long, title: String) = withContext(Dispatchers.IO) {
        val lastBlock = blockDao.getLatestBlock()
        val previousHash = lastBlock?.let {
            BlockchainManager.computeHash("${it.previousHash}|${it.contentHash}|${it.action}|${it.timestamp}|${it.signature}")
        } ?: "0"

        val contentHash = BlockchainManager.computeHash("ITEM_ID:$itemId|TITLE:$title|TIMESTAMP:${System.currentTimeMillis()}")
        val timestamp = System.currentTimeMillis()
        val signature = BlockchainManager.signBlock(previousHash, contentHash, action, timestamp)

        val block = BlockEntity(
            previousHash = previousHash,
            contentHash = contentHash,
            action = action,
            timestamp = timestamp,
            signature = signature
        )
        blockDao.insertBlock(block)
    }

    suspend fun recordPinChangeBlock() = withContext(Dispatchers.IO) {
        val lastBlock = blockDao.getLatestBlock()
        val previousHash = lastBlock?.let {
            BlockchainManager.computeHash("${it.previousHash}|${it.contentHash}|${it.action}|${it.timestamp}|${it.signature}")
        } ?: "0"

        val action = "PIN_CHANGE"
        val timestamp = System.currentTimeMillis()
        val contentHash = BlockchainManager.computeHash("ACTION:$action|TIMESTAMP:$timestamp")
        val signature = BlockchainManager.signBlock(previousHash, contentHash, action, timestamp)

        val block = BlockEntity(
            previousHash = previousHash,
            contentHash = contentHash,
            action = action,
            timestamp = timestamp,
            signature = signature
        )
        blockDao.insertBlock(block)
    }

    suspend fun recordImportBlock(source: String, itemCount: Int) = withContext(Dispatchers.IO) {
        val lastBlock = blockDao.getLatestBlock()
        val previousHash = lastBlock?.let {
            BlockchainManager.computeHash("${it.previousHash}|${it.contentHash}|${it.action}|${it.timestamp}|${it.signature}")
        } ?: "0"

        val action = "VAULT_IMPORT"
        val timestamp = System.currentTimeMillis()
        val contentHash = BlockchainManager.computeHash("ACTION:$action|SOURCE:$source|ITEMS:$itemCount|TIMESTAMP:$timestamp")
        val signature = BlockchainManager.signBlock(previousHash, contentHash, action, timestamp)

        val block = BlockEntity(
            previousHash = previousHash,
            contentHash = contentHash,
            action = action,
            timestamp = timestamp,
            signature = signature
        )
        blockDao.insertBlock(block)
    }

    suspend fun recordExportBackupBlock(format: String, itemCount: Int) = withContext(Dispatchers.IO) {
        val lastBlock = blockDao.getLatestBlock()
        val previousHash = lastBlock?.let {
            BlockchainManager.computeHash("${it.previousHash}|${it.contentHash}|${it.action}|${it.timestamp}|${it.signature}")
        } ?: "0"

        val action = "VAULT_EXPORT"
        val timestamp = System.currentTimeMillis()
        val contentHash = BlockchainManager.computeHash("ACTION:$action|FORMAT:$format|ITEMS:$itemCount|TIMESTAMP:$timestamp")
        val signature = BlockchainManager.signBlock(previousHash, contentHash, action, timestamp)

        val block = BlockEntity(
            previousHash = previousHash,
            contentHash = contentHash,
            action = action,
            timestamp = timestamp,
            signature = signature
        )
        blockDao.insertBlock(block)
    }

    suspend fun recordBiometricToggleBlock(enabled: Boolean) = withContext(Dispatchers.IO) {
        val lastBlock = blockDao.getLatestBlock()
        val previousHash = lastBlock?.let {
            BlockchainManager.computeHash("${it.previousHash}|${it.contentHash}|${it.action}|${it.timestamp}|${it.signature}")
        } ?: "0"

        val action = "BIOMETRIC_TOGGLE"
        val timestamp = System.currentTimeMillis()
        val contentHash = BlockchainManager.computeHash("ACTION:$action|ENABLED:$enabled|TIMESTAMP:$timestamp")
        val signature = BlockchainManager.signBlock(previousHash, contentHash, action, timestamp)

        val block = BlockEntity(
            previousHash = previousHash,
            contentHash = contentHash,
            action = action,
            timestamp = timestamp,
            signature = signature
        )
        blockDao.insertBlock(block)
    }

    suspend fun recordRecoveryKitBlock() = withContext(Dispatchers.IO) {
        val lastBlock = blockDao.getLatestBlock()
        val previousHash = lastBlock?.let {
            BlockchainManager.computeHash("${it.previousHash}|${it.contentHash}|${it.action}|${it.timestamp}|${it.signature}")
        } ?: "0"

        val action = "RECOVERY_KIT_GENERATE"
        val timestamp = System.currentTimeMillis()
        val contentHash = BlockchainManager.computeHash("ACTION:$action|TIMESTAMP:$timestamp")
        val signature = BlockchainManager.signBlock(previousHash, contentHash, action, timestamp)

        val block = BlockEntity(
            previousHash = previousHash,
            contentHash = contentHash,
            action = action,
            timestamp = timestamp,
            signature = signature
        )
        blockDao.insertBlock(block)
    }

    suspend fun recordPinCreateBlock() = withContext(Dispatchers.IO) {
        val lastBlock = blockDao.getLatestBlock()
        val previousHash = lastBlock?.let {
            BlockchainManager.computeHash("${it.previousHash}|${it.contentHash}|${it.action}|${it.timestamp}|${it.signature}")
        } ?: "0"

        val action = "PIN_CREATE"
        val timestamp = System.currentTimeMillis()
        val contentHash = BlockchainManager.computeHash("ACTION:$action|GENESIS_SETUP|TIMESTAMP:$timestamp")
        val signature = BlockchainManager.signBlock(previousHash, contentHash, action, timestamp)

        val block = BlockEntity(
            previousHash = previousHash,
            contentHash = contentHash,
            action = action,
            timestamp = timestamp,
            signature = signature
        )
        blockDao.insertBlock(block)
    }

    fun getAllPasswords(): Flow<List<DecryptedPasswordItem>> {
        return passwordDao.getAllPasswords().map { list ->
            list.map { entity -> entity.toDecrypted() }
        }.flowOn(Dispatchers.IO)
    }

    fun getPasswordsByCategory(category: String): Flow<List<DecryptedPasswordItem>> {
        return if (category == "All") {
            getAllPasswords()
        } else {
            passwordDao.getPasswordsByCategory(category).map { list ->
                list.map { entity -> entity.toDecrypted() }
            }.flowOn(Dispatchers.IO)
        }
    }

    fun searchPasswords(query: String): Flow<List<DecryptedPasswordItem>> {
        return passwordDao.searchPasswords(query).map { list ->
            list.map { entity -> entity.toDecrypted() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPasswordById(id: Long): DecryptedPasswordItem? = withContext(Dispatchers.IO) {
        passwordDao.getPasswordById(id)?.toDecrypted()
    }

    suspend fun getPasswordsByDomain(domain: String): List<DecryptedPasswordItem> = withContext(Dispatchers.IO) {
        passwordDao.getPasswordsByDomain(domain).map { it.toDecrypted() }
    }

    suspend fun savePassword(
        id: Long = 0L,
        title: String,
        username: String,
        plainPassword: String,
        websiteUrl: String,
        category: String,
        notes: String,
        isFavorite: Boolean = false,
        expiryDays: Int = 0,
        totpSecret: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val encryptedPassword = KeystoreManager.encrypt(plainPassword)
        val encryptedTotp = if (totpSecret.isNullOrEmpty()) null else KeystoreManager.encrypt(totpSecret)
        
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
            isFavorite = isFavorite,
            expiryDays = expiryDays,
            totpSecret = encryptedTotp
        )
        val rowId = passwordDao.insertPassword(entity)
        val finalId = if (id == 0L) rowId else id
        val savedEntity = passwordDao.getPasswordById(finalId)
        if (savedEntity != null) {
            recordBlock(if (id == 0L) "CREATE" else "UPDATE", savedEntity)
        }
        rowId
    }

    suspend fun toggleFavorite(item: DecryptedPasswordItem) {
        val entity = passwordDao.getPasswordById(item.id) ?: return
        val updated = entity.copy(isFavorite = !entity.isFavorite, updatedAt = System.currentTimeMillis())
        passwordDao.updatePassword(updated)
        recordBlock("UPDATE", updated)
    }

    suspend fun deletePassword(id: Long) {
        val entity = passwordDao.getPasswordById(id)
        if (entity != null) {
            passwordDao.deletePasswordById(id)
            recordBlock("DELETE", entity)
        }
    }

    suspend fun getRawEntities(): List<PasswordEntity> {
        return passwordDao.getAllPasswordsSync()
    }

    suspend fun importRawEntities(entities: List<PasswordEntity>) {
        passwordDao.insertPasswords(entities)
    }

    private fun PasswordEntity.toDecrypted(): DecryptedPasswordItem {
        val decryptedTotp = try {
            if (totpSecret.isNullOrEmpty()) null 
            else if (totpSecret.contains("]")) KeystoreManager.decrypt(totpSecret)
            else totpSecret // Fallback for old plaintext secrets
        } catch (e: Exception) {
            KLog.w("Failed to decrypt TOTP secret for password ID $id: ${e.message}")
            null // Return null on decryption failure to prevent exposing raw ciphertext
        }

        val decryptedPassword = try {
            KeystoreManager.decrypt(encryptedPassword)
        } catch (e: Exception) {
            KLog.e("Failed to decrypt password for ID $id", e)
            "[Decryption Error]"
        }

        return DecryptedPasswordItem(
            id = id,
            title = title,
            username = username,
            plaintextPassword = decryptedPassword,
            websiteUrl = websiteUrl,
            category = category,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isFavorite = isFavorite,
            expiryDays = expiryDays,
            totpSecret = decryptedTotp
        )
    }
}
