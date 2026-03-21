package morse.android.practice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import morse.core.MorseAlphabet
import morse.core.TimingEngine

data class TouchpadSegment(
    val symbol: Char, // '.' or '-'
    val durationMs: Long,
)

data class LetterGroup(
    val segments: List<TouchpadSegment>,
    val morse: String,
    val decoded: String,
)

enum class GapType { LETTER, WORD }

class TouchpadState(private val timing: TimingEngine) {

    val dotDashThresholdMs: Long
        get() = (timing.dotDurationMs + timing.dashDurationMs) / 2

    val letterGapThresholdMs: Long
        get() = timing.dashDurationMs * 5

    val wordGapThresholdMs: Long
        get() = timing.dashDurationMs * 10

    private val _segments = mutableStateListOf<TouchpadSegment>()
    private val _letterBreaks = mutableStateListOf<Int>()
    private val _wordGapBreaks = mutableStateListOf<Int>()

    var answer: String by mutableStateOf("")
        private set

    var letterGroups: List<LetterGroup> by mutableStateOf(emptyList())
        private set

    fun recordPress(durationMs: Long) {
        val symbol = if (durationMs < dotDashThresholdMs) '.' else '-'
        _segments.add(TouchpadSegment(symbol, durationMs))
        rebuildAnswer()
    }

    fun insertLetterGap() {
        if (_segments.isEmpty()) return
        val lastBreak = _letterBreaks.lastOrNull() ?: -1
        if (lastBreak < _segments.size - 1) {
            _letterBreaks.add(_segments.size - 1)
            rebuildAnswer()
        }
    }

    fun onGapElapsed(type: GapType) {
        if (_segments.isEmpty()) return
        when (type) {
            GapType.LETTER -> insertLetterGap()
            GapType.WORD -> {
                insertLetterGap()
                val lastBreak = _letterBreaks.lastOrNull()
                if (lastBreak != null && lastBreak !in _wordGapBreaks) {
                    _wordGapBreaks.add(lastBreak)
                    rebuildAnswer()
                }
            }
        }
    }

    fun deleteLast() {
        if (_segments.isEmpty()) return
        val lastIdx = _segments.size - 1
        if (_letterBreaks.isNotEmpty() && _letterBreaks.last() == lastIdx) {
            val removedBreak = _letterBreaks.removeAt(_letterBreaks.size - 1)
            _wordGapBreaks.remove(removedBreak)
        }
        _segments.removeAt(_segments.size - 1)
        while (_letterBreaks.isNotEmpty() && _letterBreaks.last() >= _segments.size) {
            val removedBreak = _letterBreaks.removeAt(_letterBreaks.size - 1)
            _wordGapBreaks.remove(removedBreak)
        }
        rebuildAnswer()
    }

    fun clear() {
        _segments.clear()
        _letterBreaks.clear()
        _wordGapBreaks.clear()
        answer = ""
        letterGroups = emptyList()
    }

    private fun rebuildAnswer() {
        if (_segments.isEmpty()) {
            answer = ""
            letterGroups = emptyList()
            return
        }

        val groups = mutableListOf<LetterGroup>()
        val sortedBreaks = _letterBreaks.sorted()
        var start = 0
        for (breakIdx in sortedBreaks) {
            if (breakIdx >= start && breakIdx < _segments.size) {
                val segs = _segments.subList(start, breakIdx + 1).toList()
                val morse = segs.map { it.symbol }.joinToString("")
                groups.add(LetterGroup(segs, morse, MorseAlphabet.decodeToken(morse)))
                start = breakIdx + 1
            }
        }
        if (start < _segments.size) {
            val segs = _segments.subList(start, _segments.size).toList()
            val morse = segs.map { it.symbol }.joinToString("")
            groups.add(LetterGroup(segs, morse, MorseAlphabet.decodeToken(morse)))
        }

        letterGroups = groups

        // Build canonical answer with " / " at word gaps
        val parts = mutableListOf<String>()
        var groupIdx = 0
        for (breakIdx in sortedBreaks) {
            if (groupIdx < groups.size) {
                parts.add(groups[groupIdx].morse)
                groupIdx++
                if (breakIdx in _wordGapBreaks) {
                    parts.add("/")
                }
            }
        }
        if (groupIdx < groups.size) {
            parts.add(groups[groupIdx].morse)
        }
        answer = parts.joinToString(" ")
    }
}
