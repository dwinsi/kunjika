package com.keyfortress.app.core.generator

import java.security.SecureRandom

data class PasswordGeneratorConfig(
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = true,
    val customSymbols: String = "!@#$%^&*()_"
)

object PasswordGenerator {
    private val secureRandom = SecureRandom()

    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val AMBIGUOUS = "0O1lI"

    fun generate(config: PasswordGeneratorConfig): String {
        val uppercasePool = if (config.excludeAmbiguous) UPPERCASE.filter { it !in AMBIGUOUS } else UPPERCASE
        val lowercasePool = if (config.excludeAmbiguous) LOWERCASE.filter { it !in AMBIGUOUS } else LOWERCASE
        val numberPool = if (config.excludeAmbiguous) NUMBERS.filter { it !in AMBIGUOUS } else NUMBERS
        val symbolPool = config.customSymbols

        val allowedPools = mutableListOf<String>()
        if (config.includeUppercase && uppercasePool.isNotEmpty()) allowedPools.add(uppercasePool)
        if (config.includeLowercase && lowercasePool.isNotEmpty()) allowedPools.add(lowercasePool)
        if (config.includeNumbers && numberPool.isNotEmpty()) allowedPools.add(numberPool)
        if (config.includeSymbols && symbolPool.isNotEmpty()) allowedPools.add(symbolPool)

        if (allowedPools.isEmpty()) {
            return generatePin(config.length.coerceIn(4, 12))
        }

        val allChars = allowedPools.joinToString("")
        val passwordChars = ArrayList<Char>(config.length)

        // Ensure at least one character from each selected pool is present
        for (pool in allowedPools) {
            if (passwordChars.size < config.length) {
                passwordChars.add(pool[secureRandom.nextInt(pool.length)])
            }
        }

        // Fill the rest randomly from the full character pool
        while (passwordChars.size < config.length) {
            passwordChars.add(allChars[secureRandom.nextInt(allChars.length)])
        }

        // Fisher-Yates Shuffle using SecureRandom
        for (i in passwordChars.size - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = temp
        }

        return String(passwordChars.toCharArray())
    }

    fun generatePin(length: Int = 6): String {
        val chars = CharArray(length)
        for (i in 0 until length) {
            chars[i] = NUMBERS[secureRandom.nextInt(NUMBERS.length)]
        }
        return String(chars)
    }
}
