package com.keyfortress.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.keyfortress.app.core.security.KeystoreManager
import com.keyfortress.app.data.local.history.PasswordHistoryDao
import com.keyfortress.app.data.local.history.PasswordHistoryEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [PasswordEntity::class, PasswordHistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun passwordDao(): PasswordDao
    abstract fun historyDao(): PasswordHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "keyfortress_encrypted.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `password_history` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`encryptedPassword` TEXT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `passwords` ADD COLUMN `expiryDays` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Initialize SQLCipher native libraries
                System.loadLibrary("sqlcipher")

                val passphrase = KeystoreManager.getDatabasePassphrase()
                val factory = SupportOpenHelperFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME,
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
