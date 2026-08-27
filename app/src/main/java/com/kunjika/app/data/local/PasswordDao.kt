package com.keyfortress.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllPasswords(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE category = :category ORDER BY isFavorite DESC, updatedAt DESC")
    fun getPasswordsByCategory(category: String): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE id = :id LIMIT 1")
    suspend fun getPasswordById(id: Long): PasswordEntity?

    @Query("SELECT * FROM passwords WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR websiteUrl LIKE '%' || :query || '%' ORDER BY isFavorite DESC, updatedAt DESC")
    fun searchPasswords(query: String): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE websiteUrl LIKE '%' || :domain || '%' OR title LIKE '%' || :domain || '%'")
    suspend fun getPasswordsByDomain(domain: String): List<PasswordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: PasswordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPasswords(passwords: List<PasswordEntity>)

    @Update
    suspend fun updatePassword(password: PasswordEntity)

    @Delete
    suspend fun deletePassword(password: PasswordEntity)

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: Long)

    @Query("SELECT COUNT(*) FROM passwords")
    fun getPasswordCount(): Flow<Int>

    @Query("SELECT * FROM passwords")
    suspend fun getAllPasswordsSync(): List<PasswordEntity>
}
