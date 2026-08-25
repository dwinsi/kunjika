package com.keyfortress.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyfortress.app.core.backup.BackupManager
import com.keyfortress.app.core.pdf.RecoveryPdfManager
import com.keyfortress.app.core.security.SecurityManager
import com.keyfortress.app.data.preferences.UserPreferences
import com.keyfortress.app.data.repository.PasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    val pinHint: StateFlow<String> = userPreferences.pinHint
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

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

    fun setPinHint(hint: String) {
        viewModelScope.launch {
            userPreferences.setPinHint(hint)
        }
    }

    fun generateRecoveryKit(context: Context, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val hint = userPreferences.pinHint.first()
            val passphrase = userPreferences.getOrCreateDatabasePassphrase()
            val recoveryKey = android.util.Base64.encodeToString(passphrase, android.util.Base64.NO_WRAP)
                .take(16).uppercase() // 16 char short recovery code for convenience

            RecoveryPdfManager.generateRecoveryKit(
                context = context,
                pinHint = hint,
                recoveryKey = recoveryKey,
                onComplete = { file ->
                    onComplete(file)
                    // Schedule cleanup after 5 minutes to ensure user has time to share/print
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(5 * 60 * 1000L)
                        clearRecoveryKit(context)
                    }
                }
            )
        }
    }

    fun clearRecoveryKit(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(context.cacheDir, "KeyFortress_Recovery_Kit.pdf")
            if (file.exists()) {
                file.delete()
            }
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

    fun exportToFile(context: Context, uri: Uri, passphrase: String) {
        viewModelScope.launch {
            try {
                val entities = passwordRepository.getRawEntities()
                val exportJson = BackupManager.exportEncryptedBackup(entities, passphrase)
                
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(exportJson.toByteArray())
                    }
                }
                _backupStatus.value = "Backup saved to file: ${entities.size} items"
            } catch (e: Exception) {
                _backupStatus.value = "Export to file failed: ${e.localizedMessage}"
            }
        }
    }

    fun importFromFile(context: Context, uri: Uri, passphrase: String) {
        viewModelScope.launch {
            try {
                val importJson = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().readText()
                    }
                } ?: throw Exception("Could not read file")

                val entities = BackupManager.importEncryptedBackup(importJson, passphrase)
                passwordRepository.importRawEntities(entities)
                _backupStatus.value = "Imported ${entities.size} passwords from file"
            } catch (e: Exception) {
                _backupStatus.value = "Import from file failed. Check passphrase."
            }
        }
    }

    fun clearStatus() {
        _backupStatus.value = null
    }
}
