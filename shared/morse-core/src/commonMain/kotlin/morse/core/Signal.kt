package morse.core

data class Signal(
    val type: SignalType,
    val durationMs: Long,
)

enum class SignalType {
    DOT,
    DASH,
    LETTER_GAP,
    WORD_GAP,
    SILENCE,
}
