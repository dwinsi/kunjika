package com.keyfortress.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keyfortress_settings")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_MASTER_PIN_HASH = stringPreferencesKey("master_pin_hash")
        private val KEY_IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        private val KEY_AUTO_LOCK_TIMEOUT_SEC = intPreferencesKey("auto_lock_timeout_sec")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_LOCK_ON_EXIT = booleanPreferencesKey("lock_on_exit")
        private val KEY_LAST_BACKGROUND_TIME = stringPreferencesKey("last_background_time")
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

    suspend fun setMasterPin(pin: String) {
        val hash = hashPin(pin)
        context.dataStore.edit { preferences ->
            preferences[KEY_MASTER_PIN_HASH] = hash
        }
    }

    suspend fun verifyMasterPin(pin: String): Boolean {
        var isValid = false
        val enteredHash = hashPin(pin)
        context.dataStore.edit { preferences ->
            val storedHash = preferences[KEY_MASTER_PIN_HASH]
            isValid = (storedHash == enteredHash)
        }
        return isValid
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

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
