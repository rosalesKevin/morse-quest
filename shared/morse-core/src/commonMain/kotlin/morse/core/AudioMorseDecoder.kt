package morse.core

/**
 * Accepts normalized AudioToneEvent values and converts them to Morse symbols using
 * timing thresholds derived from a TimingEngine.
 *
 * Detection approach: amplitude-threshold-based (tone present = amplitude > threshold).
 * Classification: dot/dash midpoint threshold; letter/word gap midpoint threshold.
 */
class AudioMorseDecoder(
    private val timingEngine: TimingEngine,
) {
    // Dot/dash boundary: midpoint between dot and dash durations
    private val dotDashThresholdMs: Long
        get() = (timingEngine.dotDurationMs + timingEngine.dashDurationMs) / 2

    // Silence boundary: midpoint between intra-char gap and letter gap
    private val intraLetterThresholdMs: Long
        get() = (timingEngine.intraCharacterGapMs + timingEngine.letterGapMs) / 2

    // Letter/word gap boundary: midpoint between letter gap and word gap
    private val letterWordThresholdMs: Long
        get() = (timingEngine.letterGapMs + timingEngine.wordGapMs) / 2

    // Current pattern buffer (dots and dashes for the letter being received)
    private val currentPattern = StringBuilder()

    // Completed letter patterns for the word in progress
    private val currentWordPatterns = mutableListOf<String>()

    // Completed words (each word is a list of letter patterns)
    private val completedWords = mutableListOf<List<String>>()

    /**
     * Feed one tone or silence event. Returns the current decode state after the event.
     */
    fun consume(event: AudioToneEvent): LiveDecodeState {
        if (event.isTone) {
            val symbol = if (event.durationMs < dotDashThresholdMs) '.' else '-'
            currentPattern.append(symbol)
        } else {
            when {
                event.durationMs >= letterWordThresholdMs -> {
                    flushPatternToWord()
                    flushWordToCompleted()
                }
                event.durationMs >= intraLetterThresholdMs -> {
                    flushPatternToWord()
                }
                // intra-character silence — no action needed
            }
        }
        return buildState()
    }

    /**
     * Flush any pending partial pattern and word into the output and return the final state.
     * Call when stopping listening to capture the last buffered content.
     *
     * **Note:** Does not clear accumulated state. Call [reset] after [flush] before restarting
     * a new decode session, or accumulated output will carry over.
     */
    fun flush(): LiveDecodeState {
        flushPatternToWord()
        flushWordToCompleted()
        return buildState()
    }

    /** Reset all internal state. */
    fun reset() {
        currentPattern.clear()
        currentWordPatterns.clear()
        completedWords.clear()
    }

    // ── private helpers ────────────────────────────────────────────────

    private fun flushPatternToWord() {
        if (currentPattern.isNotEmpty()) {
            currentWordPatterns.add(currentPattern.toString())
            currentPattern.clear()
        }
    }

    private fun flushWordToCompleted() {
        if (currentWordPatterns.isNotEmpty()) {
            completedWords.add(currentWordPatterns.toList())
            currentWordPatterns.clear()
        }
    }

    private fun buildState(): LiveDecodeState {
        val morse = buildMorseText()
        val decoded = buildDecodedText()
        return LiveDecodeState(morseText = morse, decodedText = decoded)
    }

    private fun buildMorseText(): String = buildString {
        completedWords.forEachIndexed { wi, word ->
            if (wi > 0) append(" / ")
            word.forEachIndexed { li, pattern ->
                if (li > 0) append(' ')
                append(pattern)
            }
        }
        val hasPending = currentWordPatterns.isNotEmpty() || currentPattern.isNotEmpty()
        if (hasPending) {
            if (completedWords.isNotEmpty()) append(" / ")
            currentWordPatterns.forEachIndexed { li, pattern ->
                if (li > 0) append(' ')
                append(pattern)
            }
            if (currentPattern.isNotEmpty()) {
                if (currentWordPatterns.isNotEmpty()) append(' ')
                append(currentPattern)
            }
        }
    }

    private fun buildDecodedText(): String = buildString {
        completedWords.forEachIndexed { wi, word ->
            if (wi > 0) append(' ')
            word.forEach { pattern -> append(MorseAlphabet.decodeToken(pattern)) }
        }
        if (currentWordPatterns.isNotEmpty()) {
            if (completedWords.isNotEmpty()) append(' ')
            currentWordPatterns.forEach { pattern -> append(MorseAlphabet.decodeToken(pattern)) }
        }
    }
}
