package com.keyfortress.app.core.generator

import java.security.SecureRandom

data class PassphraseConfig(
    val wordCount: Int = 4,
    val separator: String = "-",
    val capitalize: Boolean = true,
    val includeNumber: Boolean = true
)

object PassphraseGenerator {
    private val secureRandom = SecureRandom()

    fun generate(config: PassphraseConfig): String {
        val wordList = WordList.words
        val chosenWords = mutableListOf<String>()

        for (i in 0 until config.wordCount) {
            val randomWord = wordList[secureRandom.nextInt(wordList.size)]
            val formatted = if (config.capitalize) {
                randomWord.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            } else {
                randomWord
            }
            chosenWords.add(formatted)
        }

        if (config.includeNumber && chosenWords.isNotEmpty()) {
            val insertIndex = secureRandom.nextInt(chosenWords.size)
            val randomNum = secureRandom.nextInt(100)
            chosenWords[insertIndex] = "${chosenWords[insertIndex]}$randomNum"
        }

        return chosenWords.joinToString(config.separator)
    }
}
