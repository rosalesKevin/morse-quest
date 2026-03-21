package morse.core

data class LiveDecodeState(
    val morseText: String = "",
    val decodedText: String = "",
    val isListening: Boolean = false,
    val lastError: String? = null,
)
