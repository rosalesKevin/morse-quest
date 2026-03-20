package morse.core

object MorseDecoder {
    fun decode(morse: String): String {
        return normalizeWordPatterns(morse)
            .map { wordPatterns ->
                wordPatterns.joinToString(separator = "") { pattern -> MorseAlphabet.decodeToken(pattern) }
            }
            .joinToString(separator = " ")
    }

    fun decodeSignals(signals: List<Signal>): String {
        val words = mutableListOf<String>()
        val currentWord = mutableListOf<String>()
        val currentPattern = StringBuilder()

        fun flushPattern() {
            if (currentPattern.isNotEmpty()) {
                currentWord += MorseAlphabet.decodeToken(currentPattern.toString())
                currentPattern.clear()
            }
        }

        fun flushWord() {
            flushPattern()
            if (currentWord.isNotEmpty()) {
                words += currentWord.joinToString(separator = "")
                currentWord.clear()
            }
        }

        signals.forEach { signal ->
            when (signal.type) {
                SignalType.DOT -> currentPattern.append('.')
                SignalType.DASH -> currentPattern.append('-')
                SignalType.SILENCE -> Unit
                SignalType.LETTER_GAP -> flushPattern()
                SignalType.WORD_GAP -> flushWord()
            }
        }

        flushWord()
        return words.joinToString(separator = " ")
    }

    private fun normalizeWordPatterns(morse: String): List<List<String>> {
        val normalized = morse
            .replace(Regex("\\s*/\\s*"), " / ")
            .trim()

        if (normalized.isEmpty()) {
            return emptyList()
        }

        return normalized
            .split(Regex("\\s+/\\s+|\\s{2,}"))
            .mapNotNull { word ->
                val patterns = word
                    .trim()
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                patterns.takeIf { it.isNotEmpty() }
            }
    }
}
