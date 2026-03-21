package morse.android.practice

import androidx.compose.runtime.mutableStateListOf
import morse.core.TimingEngine

data class TapVisualSegment(
    val symbol: Char,
    val durationMs: Long,
    val matchesHint: Boolean,
)

class MorseTapComposer(
    private val timingEngine: TimingEngine,
    private val expectedPattern: String = "",
) {
    private val _segments = mutableStateListOf<TapVisualSegment>()
    val segments: List<TapVisualSegment> = _segments

    val answer: String
        get() = _segments.joinToString(separator = "") { it.symbol.toString() }

    val firstMismatchIndex: Int
        get() = _segments.indexOfFirst { !it.matchesHint }

    fun recordPress(durationMs: Long) {
        val symbol = if (durationMs < dashThresholdMs()) '.' else '-'
        val index = _segments.size
        val matchesHint = expectedPattern.getOrNull(index)?.let { it == symbol } ?: true
        _segments += TapVisualSegment(
            symbol = symbol,
            durationMs = durationMs,
            matchesHint = matchesHint,
        )
    }

    fun clear() {
        _segments.clear()
    }

    private fun dashThresholdMs(): Long =
        (timingEngine.dotDurationMs + timingEngine.dashDurationMs) / 2
}
