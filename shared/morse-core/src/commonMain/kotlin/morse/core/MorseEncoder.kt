package morse.core

class MorseEncoder(
    private val timingEngine: TimingEngine,
) {
    fun encode(text: String): String {
        return normalizeWords(text)
            .mapNotNull(::encodeWord)
            .joinToString(" / ")
    }

    fun encodeToSignals(text: String): List<Signal> {
        val words = encode(text)
            .split(" / ")
            .filter { it.isNotBlank() }

        val signals = mutableListOf<Signal>()
        words.forEachIndexed { wordIndex, word ->
            val letters = word.split(' ').filter { it.isNotBlank() }
            letters.forEachIndexed { letterIndex, pattern ->
                pattern.forEachIndexed { symbolIndex, symbol ->
                    signals += when (symbol) {
                        '.' -> Signal(SignalType.DOT, timingEngine.dotDurationMs)
                        '-' -> Signal(SignalType.DASH, timingEngine.dashDurationMs)
                        else -> error("Unsupported Morse symbol: $symbol")
                    }

                    if (symbolIndex < pattern.lastIndex) {
                        signals += Signal(SignalType.SILENCE, timingEngine.intraCharacterGapMs)
                    }
                }

                if (letterIndex < letters.lastIndex) {
                    signals += Signal(SignalType.LETTER_GAP, timingEngine.letterGapMs)
                }
            }

            if (wordIndex < words.lastIndex) {
                signals += Signal(SignalType.WORD_GAP, timingEngine.wordGapMs)
            }
        }

        return signals
    }

    private fun encodeWord(word: String): String? {
        MorseAlphabet.prosigns[word]?.let { return it }

        val encodedCharacters = word
            .mapNotNull { character -> MorseAlphabet.characters[character] }

        return encodedCharacters.takeIf { it.isNotEmpty() }?.joinToString(" ")
    }

    private fun normalizeWords(text: String): List<String> {
        return text
            .uppercase()
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }
}
