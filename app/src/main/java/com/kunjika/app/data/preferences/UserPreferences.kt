package com.kunjika.app.data.preferences

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kunjika.app.core.security.KeystoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kunjika_settings")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_MASTER_PIN_HASH = stringPreferencesKey("master_pin_hash")
        private val KEY_MASTER_PIN_SALT = stringPreferencesKey("master_pin_salt")
        private val KEY_ENCRYPTED_DB_PASSPHRASE = stringPreferencesKey("encrypted_db_passphrase")
        private val KEY_BIOMETRIC_ENCRYPTED_PIN = stringPreferencesKey("biometric_encrypted_pin")
        private val KEY_BIOMETRIC_IV = stringPreferencesKey("biometric_iv")
        private val KEY_IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        private val KEY_AUTO_LOCK_TIMEOUT_SEC = intPreferencesKey("auto_lock_timeout_sec")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_LOCK_ON_EXIT = booleanPreferencesKey("lock_on_exit")
        private val KEY_LOCK_VAULT_ON_TAB_SELECT = booleanPreferencesKey("lock_vault_on_tab_select")
        private val KEY_LAST_BACKGROUND_TIME = stringPreferencesKey("last_background_time")
        private val KEY_PIN_HINT = stringPreferencesKey("pin_hint")
        private val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val KEY_HAS_SEEN_GENERATOR_TOUR = booleanPreferencesKey("has_seen_generator_tour")
        private val KEY_HAS_SEEN_VAULT_TOUR = booleanPreferencesKey("has_seen_vault_tour")
        private val KEY_LAST_BACKUP_TIMESTAMP = stringPreferencesKey("last_backup_timestamp")
        private val KEY_CHANGES_SINCE_BACKUP = intPreferencesKey("changes_since_backup")
        private val KEY_DURESS_PIN_HASH = stringPreferencesKey("duress_pin_hash")
        private val KEY_DURESS_PIN_SALT = stringPreferencesKey("duress_pin_salt")
        private val KEY_PIN_FAILURES = intPreferencesKey("pin_failures")
        private val KEY_PIN_LOCKOUT_UNTIL = longPreferencesKey("pin_lockout_until")

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 600000
        private const val LEGACY_PIN_ITERATIONS = 100000
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

    val lockVaultOnTabSelect: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCK_VAULT_ON_TAB_SELECT] ?: true
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

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_FIRST_LAUNCH] ?: true
    }

    val hasSeenGeneratorTour: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_SEEN_GENERATOR_TOUR] ?: false
    }

    val hasSeenVaultTour: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_SEEN_VAULT_TOUR] ?: false
    }

    val changesSinceBackup: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_CHANGES_SINCE_BACKUP] ?: 0
    }

    val isDuressPinSet: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[KEY_DURESS_PIN_HASH].isNullOrEmpty()
    }

    suspend fun setMasterPin(pin: String) {
        val salt = generateSalt()
        val hash = "v2:${hashPin(pin, salt, ITERATIONS)}"
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
        
        return verifyStoredPin(pin, salt, storedHash)
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
            if (!enabled) {
                preferences.remove(KEY_BIOMETRIC_ENCRYPTED_PIN)
                preferences.remove(KEY_BIOMETRIC_IV)
            }
        }
    }

    suspend fun saveBiometricEncryptedPin(pin: String, cipher: Cipher) {
        try {
            val encryptedBytes = cipher.doFinal(pin.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

            context.dataStore.edit { preferences ->
                preferences[KEY_BIOMETRIC_ENCRYPTED_PIN] = encryptedBase64
                preferences[KEY_BIOMETRIC_IV] = ivBase64
            }
        } catch (_: Exception) { }
    }

    suspend fun getBiometricIv(): ByteArray? {
        val preferences = context.dataStore.data.first()
        val ivBase64 = preferences[KEY_BIOMETRIC_IV] ?: return null
        return try {
            Base64.decode(ivBase64, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun decryptPinWithBiometric(cipher: Cipher): String? {
        val preferences = context.dataStore.data.first()
        val encryptedBase64 = preferences[KEY_BIOMETRIC_ENCRYPTED_PIN] ?: return null
        return try {
            val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setLockOnExit(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOCK_ON_EXIT] = enabled
        }
    }

    suspend fun setLockVaultOnTabSelect(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOCK_VAULT_ON_TAB_SELECT] = enabled
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

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setHasSeenGeneratorTour(hasSeen: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_GENERATOR_TOUR] = hasSeen
        }
    }

    suspend fun setHasSeenVaultTour(hasSeen: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_VAULT_TOUR] = hasSeen
        }
    }

    suspend fun resetFeatureTours() {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_GENERATOR_TOUR] = false
            preferences[KEY_HAS_SEEN_VAULT_TOUR] = false
        }
    }

    suspend fun incrementChangesSinceBackup() {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_CHANGES_SINCE_BACKUP] ?: 0
            preferences[KEY_CHANGES_SINCE_BACKUP] = current + 1
        }
    }

    suspend fun resetBackupTracker() {
        context.dataStore.edit { preferences ->
            preferences[KEY_CHANGES_SINCE_BACKUP] = 0
            preferences[KEY_LAST_BACKUP_TIMESTAMP] = System.currentTimeMillis().toString()
        }
    }

    suspend fun setDuressPin(pin: String) {
        require(pin.length >= 6 && pin.all(Char::isDigit)) { "Duress PIN must contain at least 6 digits" }
        val salt = generateSalt()
        val hash = "v2:${hashPin(pin, salt, ITERATIONS)}"
        context.dataStore.edit { preferences ->
            preferences[KEY_DURESS_PIN_HASH] = hash
            preferences[KEY_DURESS_PIN_SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
        }
    }

    enum class PinMode {
        MASTER,
        DURESS,
        INVALID
    }

    suspend fun verifyPinMode(pin: String): PinMode {
        val preferences = context.dataStore.data.first()
        if ((preferences[KEY_PIN_LOCKOUT_UNTIL] ?: 0L) > System.currentTimeMillis()) return PinMode.INVALID

        val storedMasterHash = preferences[KEY_MASTER_PIN_HASH]
        val masterSaltBase64 = preferences[KEY_MASTER_PIN_SALT]
        if (!storedMasterHash.isNullOrEmpty() && !masterSaltBase64.isNullOrEmpty()) {
            val salt = Base64.decode(masterSaltBase64, Base64.NO_WRAP)
            if (verifyStoredPin(pin, salt, storedMasterHash)) {
                resetPinFailures()
                upgradePinHashIfLegacy(pin, salt, storedMasterHash, KEY_MASTER_PIN_HASH)
                return PinMode.MASTER
            }
        }

        val storedDuressHash = preferences[KEY_DURESS_PIN_HASH]
        val duressSaltBase64 = preferences[KEY_DURESS_PIN_SALT]
        if (!storedDuressHash.isNullOrEmpty() && !duressSaltBase64.isNullOrEmpty()) {
            val salt = Base64.decode(duressSaltBase64, Base64.NO_WRAP)
            if (verifyStoredPin(pin, salt, storedDuressHash)) {
                resetPinFailures()
                upgradePinHashIfLegacy(pin, salt, storedDuressHash, KEY_DURESS_PIN_HASH)
                return PinMode.DURESS
            }
        }

        context.dataStore.edit { prefs ->
            val failures = (prefs[KEY_PIN_FAILURES] ?: 0) + 1
            prefs[KEY_PIN_FAILURES] = failures
            if (failures >= 5) {
                val exponent = (failures - 5).coerceAtMost(10)
                val delayMillis = 60_000L * (1L shl exponent)
                prefs[KEY_PIN_LOCKOUT_UNTIL] = System.currentTimeMillis() + delayMillis
            }
        }
        return PinMode.INVALID
    }

    private suspend fun resetPinFailures() {
        context.dataStore.edit { prefs ->
            prefs[KEY_PIN_FAILURES] = 0
            prefs[KEY_PIN_LOCKOUT_UNTIL] = 0L
        }
    }

    private suspend fun verifyStoredPin(pin: String, salt: ByteArray, stored: String): Boolean {
        val legacy = !stored.startsWith("v2:")
        val expected = if (legacy) stored else stored.removePrefix("v2:")
        val iterations = if (legacy) LEGACY_PIN_ITERATIONS else ITERATIONS
        return expected == hashPin(pin, salt, iterations)
    }

    private suspend fun upgradePinHashIfLegacy(pin: String, salt: ByteArray, oldHash: String, key: androidx.datastore.preferences.core.Preferences.Key<String>) {
        if (oldHash.startsWith("v2:")) return
        val upgraded = "v2:${hashPin(pin, salt, ITERATIONS)}"
        context.dataStore.edit { it[key] = upgraded }
    }

    private suspend fun hashPin(pin: String, salt: ByteArray, iterations: Int = ITERATIONS): String =
        withContext(Dispatchers.Default) {
            val pinChars = pin.toCharArray()
            val spec = PBEKeySpec(pinChars, salt, iterations, KEY_LENGTH)
            try {
                val hash = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).encoded
                hash.joinToString("") { "%02x".format(it) }
            } finally {
                spec.clearPassword()
                pinChars.fill('\u0000')
            }
        }

    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }
}
