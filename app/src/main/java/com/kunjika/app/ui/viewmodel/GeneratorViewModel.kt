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

enum class GeneratorMode {
    PASSWORD,
    PASSPHRASE,
    PIN,
    HISTORY
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
    // Validation
    val isConfigValid: Boolean = true
)

class GeneratorViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<DecryptedHistoryItem>> = historyRepository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        generate()
    }

    fun setMode(mode: GeneratorMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
        if (mode != GeneratorMode.HISTORY) {
            generate()
        }
    }

    fun setLength(length: Int) {
        _uiState.value = _uiState.value.copy(length = length)
        generate()
    }

    fun setPinLength(length: Int) {
        _uiState.value = _uiState.value.copy(pinLength = length)
        generate()
    }

    fun setWordCount(count: Int) {
        _uiState.value = _uiState.value.copy(wordCount = count)
        generate()
    }

    fun setSeparator(separator: String) {
        _uiState.value = _uiState.value.copy(separator = separator)
        generate()
    }

    fun toggleUppercase() {
        val current = _uiState.value
        _uiState.value = current.copy(includeUppercase = !current.includeUppercase)
        generate()
    }

    fun toggleLowercase() {
        val current = _uiState.value
        _uiState.value = current.copy(includeLowercase = !current.includeLowercase)
        generate()
    }

    fun toggleNumbers() {
        val current = _uiState.value
        _uiState.value = current.copy(includeNumbers = !current.includeNumbers)
        generate()
    }

    fun toggleSymbols() {
        val current = _uiState.value
        _uiState.value = current.copy(includeSymbols = !current.includeSymbols)
        generate()
    }

    fun toggleExcludeAmbiguous() {
        val current = _uiState.value
        _uiState.value = current.copy(excludeAmbiguous = !current.excludeAmbiguous)
        generate()
    }

    fun toggleCapitalize() {
        val current = _uiState.value
        _uiState.value = current.copy(capitalize = !current.capitalize)
        generate()
    }

    fun toggleIncludeNumberInPassphrase() {
        val current = _uiState.value
        _uiState.value = current.copy(includeNumberInPassphrase = !current.includeNumberInPassphrase)
        generate()
    }

    fun generate() {
        val state = _uiState.value
        if (state.mode == GeneratorMode.HISTORY) return

        val selectedCount = listOf(
            state.includeUppercase,
            state.includeLowercase,
            state.includeNumbers,
            state.includeSymbols
        ).count { it }
        val isConfigValid = selectedCount >= 3

        if (!isConfigValid && state.mode == GeneratorMode.PASSWORD) {
            _uiState.value = state.copy(
                generatedPassword = "",
                strengthResult = PasswordStrengthEvaluator.evaluate(""),
                isConfigValid = false
            )
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
            GeneratorMode.HISTORY -> "" // Should not reach here
        }

        val strength = PasswordStrengthEvaluator.evaluate(newPassword)

        if (newPassword.isNotEmpty()) {
            viewModelScope.launch {
                historyRepository.addHistory(newPassword, state.mode.name)
            }
        }

        _uiState.value = state.copy(
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
