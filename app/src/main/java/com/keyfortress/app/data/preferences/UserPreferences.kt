package com.keyfortress.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.keyfortress.app.core.security.KeystoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keyfortress_settings")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_MASTER_PIN_HASH = stringPreferencesKey("master_pin_hash")
        private val KEY_MASTER_PIN_SALT = stringPreferencesKey("master_pin_salt")
        private val KEY_ENCRYPTED_DB_PASSPHRASE = stringPreferencesKey("encrypted_db_passphrase")
        private val KEY_IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        private val KEY_AUTO_LOCK_TIMEOUT_SEC = intPreferencesKey("auto_lock_timeout_sec")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_LOCK_ON_EXIT = booleanPreferencesKey("lock_on_exit")
        private val KEY_LAST_BACKGROUND_TIME = stringPreferencesKey("last_background_time")
        private val KEY_PIN_HINT = stringPreferencesKey("pin_hint")

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 100000
        private const val KEY_LENGTH = 256
    }

    val isMasterPinSet: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[KEY_MASTER_PIN_HASH].isNullOrEmpty()
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_BIOMETRIC_ENABLED] ?: true
    }

    val lockOnExit: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCK_ON_EXIT] ?: false
    }

    val autoLockTimeoutSec: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_LOCK_TIMEOUT_SEC] ?: 30 // default 30 seconds
    }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DYNAMIC_COLOR] ?: true
    }

    val useDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_THEME] ?: true
    }

    val pinHint: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_HINT] ?: ""
    }

    suspend fun setMasterPin(pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        context.dataStore.edit { preferences ->
            preferences[KEY_MASTER_PIN_HASH] = hash
            preferences[KEY_MASTER_PIN_SALT] = android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
        }
    }

    suspend fun verifyMasterPin(pin: String): Boolean {
        val preferences = context.dataStore.data.first()
        val storedHash = preferences[KEY_MASTER_PIN_HASH] ?: return false
        val saltBase64 = preferences[KEY_MASTER_PIN_SALT] ?: return false
        val salt = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
        
        val enteredHash = hashPin(pin, salt)
        return storedHash == enteredHash
    }

    suspend fun getOrCreateDatabasePassphrase(): ByteArray {
        val preferences = context.dataStore.data.first()
        val encryptedPassphrase = preferences[KEY_ENCRYPTED_DB_PASSPHRASE]

        return if (encryptedPassphrase.isNullOrEmpty()) {
            // Generate a new random 256-bit passphrase
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            val rawPassphrase = android.util.Base64.encodeToString(randomBytes, android.util.Base64.NO_WRAP)
            
            // Encrypt with Keystore and save
            val encrypted = KeystoreManager.encrypt(rawPassphrase)
            context.dataStore.edit { prefs ->
                prefs[KEY_ENCRYPTED_DB_PASSPHRASE] = encrypted
            }
            rawPassphrase.toByteArray(Charsets.UTF_8)
        } else {
            // Decrypt the existing passphrase
            val decrypted = KeystoreManager.decrypt(encryptedPassphrase)
            decrypted.toByteArray(Charsets.UTF_8)
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setLockOnExit(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOCK_ON_EXIT] = enabled
        }
    }

    suspend fun setAutoLockTimeout(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_LOCK_TIMEOUT_SEC] = seconds
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = enabled
        }
    }

    suspend fun setPinHint(hint: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PIN_HINT] = hint
        }
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }
}
