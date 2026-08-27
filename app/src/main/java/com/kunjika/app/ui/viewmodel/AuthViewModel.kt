package com.kunjika.app.ui.viewmodel

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunjika.app.core.security.BiometricKeyManager
import com.kunjika.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object Onboarding : AuthState()
    object SetupRequired : AuthState()
    object Locked : AuthState()
    object Authenticated : AuthState()
}

class AuthViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(true)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private var lastBackgroundTimestamp: Long = 0L

    init {
        checkInitialStatus()
    }

    private fun checkInitialStatus() {
        viewModelScope.launch {
            val isFirstLaunch = userPreferences.isFirstLaunch.first()
            _isBiometricEnabled.value = userPreferences.isBiometricEnabled.first()

            if (isFirstLaunch) {
                _authState.value = AuthState.Onboarding
            } else {
                val isPinSet = userPreferences.isMasterPinSet.first()
                if (!isPinSet) {
                    _authState.value = AuthState.SetupRequired
                } else {
                    _authState.value = AuthState.Locked
                }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setFirstLaunchCompleted()
            _authState.value = AuthState.SetupRequired
        }
    }

    fun setupMasterPin(pin: String) {
        if (pin.length < 4) {
            _errorMessage.value = "PIN must be at least 4 digits"
            return
        }
        viewModelScope.launch {
            userPreferences.setMasterPin(pin)
            _errorMessage.value = null
            _authState.value = AuthState.Authenticated
        }
    }

    fun unlockWithPin(pin: String) {
        viewModelScope.launch {
            val isValid = userPreferences.verifyMasterPin(pin)
            if (isValid) {
                _errorMessage.value = null
                _authState.value = AuthState.Authenticated
            } else {
                _errorMessage.value = "Incorrect Master PIN"
            }
        }
    }

    fun unlockWithBiometrics(result: BiometricPrompt.AuthenticationResult) {
        // In a more advanced implementation, we would use the cipher from result.cryptoObject
        // to decrypt the Master PIN or a session key.
        // For now, the hardware-verified success is our trigger.
        val cipher = result.cryptoObject?.cipher
        if (cipher != null) {
            _errorMessage.value = null
            _authState.value = AuthState.Authenticated
        } else {
            _errorMessage.value = "Biometric authentication failed to provide crypto object"
        }
    }

    fun getBiometricCryptoObject(): BiometricPrompt.CryptoObject? {
        return try {
            val cipher = BiometricKeyManager.getEncryptionCipher()
            BiometricPrompt.CryptoObject(cipher)
        } catch (_: Exception) {
            null
        }
    }

    fun lock() {
        viewModelScope.launch {
            val isPinSet = userPreferences.isMasterPinSet.first()
            if (isPinSet) {
                _authState.value = AuthState.Locked
            }
        }
    }

    fun onAppBackgrounded() {
        lastBackgroundTimestamp = System.currentTimeMillis()
        viewModelScope.launch {
            if (userPreferences.lockOnExit.first()) {
                lock()
            }
        }
    }

    fun onAppForegrounded() {
        if (_authState.value == AuthState.Authenticated) {
            viewModelScope.launch {
                val timeoutSec = userPreferences.autoLockTimeoutSec.first()
                if (timeoutSec > 0 && lastBackgroundTimestamp > 0) {
                    val elapsedSec = (System.currentTimeMillis() - lastBackgroundTimestamp) / 1000
                    if (elapsedSec >= timeoutSec) {
                        lock()
                    }
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
