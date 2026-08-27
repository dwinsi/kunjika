package com.keyfortress.app.core.generator

import kotlin.math.log2
import kotlin.math.pow

enum class PasswordStrength(val label: String, val level: Int) {
    VERY_WEAK("Very Weak", 0),
    WEAK("Weak", 1),
    FAIR("Fair", 2),
    STRONG("Strong", 3),
    VERY_STRONG("Very Strong", 4)
}

data class StrengthResult(
    val score: PasswordStrength,
    val entropyBits: Double,
    val crackTimeEstimate: String,
    val feedback: List<String>
)

object PasswordStrengthEvaluator {

    fun evaluate(password: String): StrengthResult {
        if (password.isEmpty()) {
            return StrengthResult(
                score = PasswordStrength.VERY_WEAK,
                entropyBits = 0.0,
                crackTimeEstimate = "Instant",
                feedback = listOf("Password is empty")
            )
        }

        var poolSize = 0
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        var hasSymbol = false

        for (c in password) {
            when {
                c.isUpperCase() -> hasUpper = true
                c.isLowerCase() -> hasLower = true
                c.isDigit() -> hasDigit = true
                else -> hasSymbol = true
            }
        }

        if (hasUpper) poolSize += 26
        if (hasLower) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSymbol) poolSize += 33

        val length = password.length
        val entropy = if (poolSize > 0) length * log2(poolSize.toDouble()) else 0.0

        val feedback = mutableListOf<String>()
        if (length < 8) feedback.add("Minimum 8 characters recommended")
        if (!hasUpper) feedback.add("Add uppercase letters")
        if (!hasLower) feedback.add("Add lowercase letters")
        if (!hasDigit) feedback.add("Add numbers")
        if (!hasSymbol) feedback.add("Add special symbols")

        val strength = when {
            entropy < 28 -> PasswordStrength.VERY_WEAK
            entropy < 45 -> PasswordStrength.WEAK
            entropy < 65 -> PasswordStrength.FAIR
            entropy < 85 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }

        val crackTime = calculateCrackTime(entropy)

        return StrengthResult(
            score = strength,
            entropyBits = Math.round(entropy * 10.0) / 10.0,
            crackTimeEstimate = crackTime,
            feedback = feedback
        )
    }

    private fun calculateCrackTime(entropy: Double): String {
        // Assume attacker can test 10^10 (10 billion) guesses per second on offline GPU cluster
        val guessesPerSec = 10.0.pow(10)
        val totalCombinations = 2.0.pow(entropy)
        val seconds = (totalCombinations / 2.0) / guessesPerSec

        return when {
            seconds < 0.01 -> "Instant"
            seconds < 60 -> "${seconds.toInt()} seconds"
            seconds < 3600 -> "${(seconds / 60).toInt()} minutes"
            seconds < 86400 -> "${(seconds / 3600).toInt()} hours"
            seconds < 31536000 -> "${(seconds / 86400).toInt()} days"
            seconds < 3153600000 -> "${(seconds / 31536000).toInt()} years"
            seconds < 3153600000000 -> "${(seconds / 3153600000).toInt()} centuries"
            else -> "Centuries+"
        }
    }
}
