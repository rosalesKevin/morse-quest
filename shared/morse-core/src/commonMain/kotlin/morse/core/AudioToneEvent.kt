package morse.core

data class AudioToneEvent(
    val isTone: Boolean,
    val durationMs: Long,
)
