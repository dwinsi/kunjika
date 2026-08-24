package com.keyfortress.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyfortress.app.core.backup.BackupManager
import com.keyfortress.app.core.security.SecurityManager
import com.keyfortress.app.data.preferences.UserPreferences
import com.keyfortress.app.data.repository.PasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SecurityStatus(
    val isRooted: Boolean = false,
    val isEmulator: Boolean = false
)

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val passwordRepository: PasswordRepository
) : ViewModel() {

    val isBiometricEnabled: StateFlow<Boolean> = userPreferences.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lockOnExit: StateFlow<Boolean> = userPreferences.lockOnExit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoLockTimeoutSec: StateFlow<Int> = userPreferences.autoLockTimeoutSec
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val useDynamicColor: StateFlow<Boolean> = userPreferences.useDynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val useDarkTheme: StateFlow<Boolean> = userPreferences.useDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val securityStatus = MutableStateFlow(
        SecurityStatus(
            isRooted = SecurityManager.isDeviceRooted(),
            isEmulator = SecurityManager.isRunningOnEmulator()
        )
    ).asStateFlow()

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setBiometricEnabled(enabled)
        }
    }

    fun setLockOnExit(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setLockOnExit(enabled)
        }
    }

    fun setAutoLockTimeout(seconds: Int) {
        viewModelScope.launch {
            userPreferences.setAutoLockTimeout(seconds)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDynamicColor(enabled)
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkTheme(enabled)
        }
    }

    fun changeMasterPin(newPin: String) {
        viewModelScope.launch {
            userPreferences.setMasterPin(newPin)
            _backupStatus.value = "Master PIN updated successfully"
        }
    }

    fun exportEncryptedBackup(passphrase: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val entities = passwordRepository.getRawEntities()
                val exportJson = BackupManager.exportEncryptedBackup(entities, passphrase)
                _backupStatus.value = "Backup generated successfully (${entities.size} items)"
                onComplete(exportJson)
            } catch (e: Exception) {
                _backupStatus.value = "Export failed: ${e.localizedMessage}"
            }
        }
    }

    fun importEncryptedBackup(backupJson: String, passphrase: String) {
        viewModelScope.launch {
            try {
                val entities = BackupManager.importEncryptedBackup(backupJson, passphrase)
                passwordRepository.importRawEntities(entities)
                _backupStatus.value = "Imported ${entities.size} passwords successfully"
            } catch (e: Exception) {
                _backupStatus.value = "Import failed. Invalid passphrase or corrupted file."
            }
        }
    }

    fun clearStatus() {
        _backupStatus.value = null
    }
}
