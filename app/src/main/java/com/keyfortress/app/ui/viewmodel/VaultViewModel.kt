package com.keyfortress.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keyfortress.app.core.generator.PasswordStrength
import com.keyfortress.app.core.generator.PasswordStrengthEvaluator
import com.keyfortress.app.data.repository.DecryptedPasswordItem
import com.keyfortress.app.data.repository.PasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SecurityAuditSummary(
    val totalPasswords: Int = 0,
    val securityScore: Int = 100,
    val weakPasswords: List<DecryptedPasswordItem> = emptyList(),
    val reusedPasswords: List<DecryptedPasswordItem> = emptyList(),
    val oldPasswords: List<DecryptedPasswordItem> = emptyList(),
    val expiredPasswords: List<DecryptedPasswordItem> = emptyList()
)

class VaultViewModel(private val repository: PasswordRepository) : ViewModel() {

    val categories = listOf("All", "Personal", "Work", "Finance", "Social", "Streaming", "Other")

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allPasswords: StateFlow<List<DecryptedPasswordItem>> = repository.getAllPasswords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPasswords: StateFlow<List<DecryptedPasswordItem>> = combine(
        allPasswords,
        _selectedCategory,
        _searchQuery
    ) { list, category, query ->
        list.filter { item ->
            val matchesCategory = (category == "All" || item.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isEmpty() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.username.contains(query, ignoreCase = true) ||
                    item.websiteUrl.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditSummary: StateFlow<SecurityAuditSummary> = allPasswords.combine(MutableStateFlow(Unit)) { list, _ ->
        computeAudit(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SecurityAuditSummary())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(item: DecryptedPasswordItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun savePassword(
        id: Long = 0L,
        title: String,
        username: String,
        plainPassword: String,
        websiteUrl: String,
        category: String,
        notes: String,
        isFavorite: Boolean = false,
        expiryDays: Int = 0
    ) {
        viewModelScope.launch {
            repository.savePassword(
                id = id,
                title = title,
                username = username,
                plainPassword = plainPassword,
                websiteUrl = websiteUrl,
                category = category,
                notes = notes,
                isFavorite = isFavorite,
                expiryDays = expiryDays
            )
        }
    }

    fun deletePassword(id: Long) {
        viewModelScope.launch {
            repository.deletePassword(id)
        }
    }

    private fun computeAudit(list: List<DecryptedPasswordItem>): SecurityAuditSummary {
        if (list.isEmpty()) return SecurityAuditSummary()

        val weak = mutableListOf<DecryptedPasswordItem>()
        val passwordCountMap = mutableMapOf<String, Int>()
        val ninetyDaysMillis = 90L * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val old = mutableListOf<DecryptedPasswordItem>()
        val expired = mutableListOf<DecryptedPasswordItem>()

        for (item in list) {
            val strength = PasswordStrengthEvaluator.evaluate(item.plaintextPassword)
            if (strength.score == PasswordStrength.VERY_WEAK || strength.score == PasswordStrength.WEAK) {
                weak.add(item)
            }

            if (item.plaintextPassword.isNotEmpty()) {
                passwordCountMap[item.plaintextPassword] = (passwordCountMap[item.plaintextPassword] ?: 0) + 1
            }

            if (now - item.updatedAt > ninetyDaysMillis) {
                old.add(item)
            }

            if (item.expiryDays > 0) {
                val expiryMillis = item.expiryDays.toLong() * 24 * 60 * 60 * 1000L
                if (now - item.updatedAt > expiryMillis) {
                    expired.add(item)
                }
            }
        }

        val reused = list.filter { (passwordCountMap[it.plaintextPassword] ?: 0) > 1 }

        var score = 100
        score -= (weak.size * 15)
        score -= (reused.size * 10)
        score -= (old.size * 5)
        score -= (expired.size * 20)
        score = score.coerceIn(0, 100)

        return SecurityAuditSummary(
            totalPasswords = list.size,
            securityScore = score,
            weakPasswords = weak,
            reusedPasswords = reused,
            oldPasswords = old,
            expiredPasswords = expired
        )
    }
}
