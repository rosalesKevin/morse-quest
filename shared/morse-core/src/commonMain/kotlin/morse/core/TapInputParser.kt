package morse.core

class TapInputParser(
    val timingEngine: TimingEngine,
    private val minimumPressDurationMs: Long = 1L,
    private val durationToleranceMs: Long = timingEngine.dotDurationMs / 2,
    private val silenceFlushThresholdMs: Long = timingEngine.wordGapMs * 3,
) {
    private var pressStartedAtMs: Long? = null
    private var lastReleaseAtMs: Long? = null
    private val currentPattern = StringBuilder()
    private val currentWord = StringBuilder()
    private val completedWords = mutableListOf<String>()

    fun onPress(timestampMs: Long) {
        pressStartedAtMs = timestampMs
    }

    fun onRelease(timestampMs: Long) {
        val startedAtMs = pressStartedAtMs ?: return
        pressStartedAtMs = null

        val durationMs = timestampMs - startedAtMs
        if (durationMs < minimumPressDurationMs) {
            lastReleaseAtMs = timestampMs
            return
        }

        val dashThresholdMs = ((timingEngine.dotDurationMs + timingEngine.dashDurationMs) / 2) + durationToleranceMs
        currentPattern.append(if (durationMs < dashThresholdMs) '.' else '-')
        lastReleaseAtMs = timestampMs
    }

    fun onIdle(timestampMs: Long): String? {
        if (pressStartedAtMs != null) {
            return null
        }

        val releasedAtMs = lastReleaseAtMs ?: return null
        val idleDurationMs = timestampMs - releasedAtMs

        if (idleDurationMs >= timingEngine.letterGapMs) {
            flushPatternToCurrentWord()
        }

        if (idleDurationMs >= timingEngine.wordGapMs) {
            flushWordToCompletedWords()
        }

        if (idleDurationMs < silenceFlushThresholdMs) {
            return null
        }

        flushPatternToCurrentWord()
        flushWordToCompletedWords()

        if (completedWords.isEmpty()) {
            return null
        }

        val decoded = completedWords.joinToString(separator = " ")
        reset()
        return decoded
    }

    fun reset() {
        pressStartedAtMs = null
        lastReleaseAtMs = null
        currentPattern.clear()
        currentWord.clear()
        completedWords.clear()
    }

    private fun flushPatternToCurrentWord() {
        if (currentPattern.isEmpty()) {
            return
        }

        currentWord.append(MorseAlphabet.decodeToken(currentPattern.toString()))
        currentPattern.clear()
    }

    private fun flushWordToCompletedWords() {
        flushPatternToCurrentWord()
        if (currentWord.isEmpty()) {
            return
        }

        completedWords += currentWord.toString()
        currentWord.clear()
    }
}
