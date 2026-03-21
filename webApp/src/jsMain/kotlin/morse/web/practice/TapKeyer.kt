package morse.web.practice

import morse.core.TapInputParser
import morse.core.TimingEngine

data class HoldFeedback(
    val durationMs: Long,
    val symbol: Char,
)

class TapKeyer(
    private val timingEngine: TimingEngine,
    private val parser: TapInputParser = TapInputParser(timingEngine),
) {
    var currentAnswer: String = ""
        private set

    var lastFeedback: HoldFeedback? = null
        private set

    private var pressStartedAtMs: Long? = null
    private var lastReleaseAtMs: Long? = null
    private var gapResolved = true
    private val currentPattern = StringBuilder()
    private val tokens = mutableListOf<String>()

    fun press(timestampMs: Long) {
        resolveIdleGap(timestampMs)
        pressStartedAtMs = timestampMs
        parser.onPress(timestampMs)
    }

    fun release(timestampMs: Long) {
        val startedAtMs = pressStartedAtMs ?: return
        pressStartedAtMs = null

        val durationMs = timestampMs - startedAtMs
        parser.onRelease(timestampMs)
        lastReleaseAtMs = timestampMs
        gapResolved = false

        if (durationMs < 1L) {
            return
        }

        val symbol = classify(durationMs)
        currentPattern.append(symbol)
        lastFeedback = HoldFeedback(durationMs = durationMs, symbol = symbol)
        updateCurrentAnswer()
    }

    fun idle(timestampMs: Long): String? {
        resolveIdleGap(timestampMs)
        updateCurrentAnswer()
        return parser.onIdle(timestampMs)
    }

    fun reset() {
        pressStartedAtMs = null
        lastReleaseAtMs = null
        gapResolved = true
        currentPattern.clear()
        tokens.clear()
        currentAnswer = ""
        lastFeedback = null
        parser.reset()
    }

    private fun resolveIdleGap(timestampMs: Long) {
        if (gapResolved) {
            return
        }

        val releasedAtMs = lastReleaseAtMs ?: return
        val idleDurationMs = timestampMs - releasedAtMs
        if (idleDurationMs < timingEngine.letterGapMs) {
            updateCurrentAnswer()
            return
        }

        if (currentPattern.isNotEmpty()) {
            tokens += currentPattern.toString()
            currentPattern.clear()
        }

        if (idleDurationMs >= timingEngine.wordGapMs && tokens.isNotEmpty() && tokens.last() != "/") {
            tokens += "/"
        }

        gapResolved = true
        updateCurrentAnswer()
    }

    private fun classify(durationMs: Long): Char {
        val dashThresholdMs = (timingEngine.dotDurationMs + timingEngine.dashDurationMs) / 2
        return if (durationMs < dashThresholdMs) '.' else '-'
    }

    private fun updateCurrentAnswer() {
        val parts = buildList {
            addAll(tokens)
            if (currentPattern.isNotEmpty()) {
                add(currentPattern.toString())
            }
        }.dropLastWhile { it == "/" }

        currentAnswer = parts.joinToString(" ")
    }
}
