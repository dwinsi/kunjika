package com.kunjika.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunjika.app.core.generator.PassphraseConfig
import com.kunjika.app.core.generator.PassphraseGenerator
import com.kunjika.app.core.generator.PasswordGenerator
import com.kunjika.app.core.generator.PasswordGeneratorConfig
import com.kunjika.app.core.generator.PasswordStrengthEvaluator
import com.kunjika.app.core.generator.StrengthResult
import com.kunjika.app.data.repository.DecryptedHistoryItem
import com.kunjika.app.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.SecureRandom

enum class GeneratorMode {
    PASSWORD,
    PASSPHRASE,
    PIN,
    TOTP
}

data class GeneratorUiState(
    val mode: GeneratorMode = GeneratorMode.PASSWORD,
    val generatedPassword: String = "",
    val strengthResult: StrengthResult = PasswordStrengthEvaluator.evaluate(""),
    // Standard password config
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = true,
    // Passphrase config
    val wordCount: Int = 4,
    val separator: String = "-",
    val capitalize: Boolean = true,
    val includeNumberInPassphrase: Boolean = true,
    // PIN config
    val pinLength: Int = 6,
    // TOTP config (Standard Google Auth 160-bit Base32 secret)
    val totpSecret: String = "",
    // Validation
    val isConfigValid: Boolean = true
)

class GeneratorViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<DecryptedHistoryItem>> = historyRepository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun generateTotpSecret(): String {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val random = SecureRandom()
        val secret = StringBuilder(32)
        for (i in 0 until 32) {
            secret.append(base32Chars[random.nextInt(base32Chars.length)])
        }
        return secret.toString()
    }

    fun setMode(mode: GeneratorMode) {
        val currentSecret = if (mode == GeneratorMode.TOTP && _uiState.value.totpSecret.isEmpty()) {
            generateTotpSecret()
        } else {
            _uiState.value.totpSecret
        }

        _uiState.value = _uiState.value.copy(
            mode = mode,
            totpSecret = currentSecret,
            generatedPassword = if (mode == GeneratorMode.TOTP) currentSecret else "",
            strengthResult = PasswordStrengthEvaluator.evaluate("")
        )
    }

    fun setCustomTotpSecret(secret: String) {
        val filtered = secret.uppercase().filter { it in "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567" }
        _uiState.value = _uiState.value.copy(
            totpSecret = filtered,
            generatedPassword = filtered
        )
    }

    fun setLength(length: Int) {
        _uiState.value = _uiState.value.copy(length = length)
    }

    fun setPinLength(length: Int) {
        _uiState.value = _uiState.value.copy(pinLength = length)
    }

    fun setWordCount(count: Int) {
        _uiState.value = _uiState.value.copy(wordCount = count)
    }

    fun setSeparator(separator: String) {
        _uiState.value = _uiState.value.copy(separator = separator)
    }

    fun toggleUppercase() {
        val current = _uiState.value
        _uiState.value = current.copy(includeUppercase = !current.includeUppercase)
        validateConfig()
    }

    fun toggleLowercase() {
        val current = _uiState.value
        _uiState.value = current.copy(includeLowercase = !current.includeLowercase)
        validateConfig()
    }

    fun toggleNumbers() {
        val current = _uiState.value
        _uiState.value = current.copy(includeNumbers = !current.includeNumbers)
        validateConfig()
    }

    fun toggleSymbols() {
        val current = _uiState.value
        _uiState.value = current.copy(includeSymbols = !current.includeSymbols)
        validateConfig()
    }

    fun toggleExcludeAmbiguous() {
        val current = _uiState.value
        _uiState.value = current.copy(excludeAmbiguous = !current.excludeAmbiguous)
    }

    fun toggleCapitalize() {
        val current = _uiState.value
        _uiState.value = current.copy(capitalize = !current.capitalize)
    }

    fun toggleIncludeNumberInPassphrase() {
        val current = _uiState.value
        _uiState.value = current.copy(includeNumberInPassphrase = !current.includeNumberInPassphrase)
    }

    private fun validateConfig() {
        val state = _uiState.value
        val selectedCount = listOf(
            state.includeUppercase,
            state.includeLowercase,
            state.includeNumbers,
            state.includeSymbols
        ).count { it }
        _uiState.value = state.copy(isConfigValid = selectedCount >= 3)
    }

    fun generate() {
        val state = _uiState.value

        if (!state.isConfigValid && state.mode == GeneratorMode.PASSWORD) {
            return
        }

        val newPassword = when (state.mode) {
            GeneratorMode.PASSWORD -> {
                PasswordGenerator.generate(
                    PasswordGeneratorConfig(
                        length = state.length,
                        includeUppercase = state.includeUppercase,
                        includeLowercase = state.includeLowercase,
                        includeNumbers = state.includeNumbers,
                        includeSymbols = state.includeSymbols,
                        excludeAmbiguous = state.excludeAmbiguous
                    )
                )
            }
            GeneratorMode.PASSPHRASE -> {
                PassphraseGenerator.generate(
                    PassphraseConfig(
                        wordCount = state.wordCount,
                        separator = state.separator,
                        capitalize = state.capitalize,
                        includeNumber = state.includeNumberInPassphrase
                    )
                )
            }
            GeneratorMode.PIN -> {
                PasswordGenerator.generatePin(state.pinLength)
            }
            GeneratorMode.TOTP -> {
                generateTotpSecret()
            }
        }

        val strength = PasswordStrengthEvaluator.evaluate(newPassword)

        if (newPassword.isNotEmpty() && state.mode != GeneratorMode.TOTP) {
            viewModelScope.launch {
                historyRepository.addHistory(newPassword, state.mode.name)
            }
        }

        _uiState.value = state.copy(
            totpSecret = if (state.mode == GeneratorMode.TOTP) newPassword else state.totpSecret,
            generatedPassword = newPassword,
            strengthResult = strength,
            isConfigValid = true
        )
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
