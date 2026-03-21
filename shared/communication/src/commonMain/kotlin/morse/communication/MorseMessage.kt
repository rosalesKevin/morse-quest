package morse.communication

import kotlinx.serialization.Serializable

/**
 * A single Morse message exchanged in a communication session.
 *
 * [morseContent] uses canonical encoding: single spaces between letters, `/` between words.
 * [wpm] is the speed at which the sender transmitted the message.
 */
@Serializable
data class MorseMessage(
    val id: String,
    val senderId: String,
    val morseContent: String,
    val decodedContent: String,
    val timestampMs: Long,
    val wpm: Int,
)
