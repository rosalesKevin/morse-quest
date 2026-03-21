package morse.communication

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MorseMessageTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val sample = MorseMessage(
        id = "msg-1",
        senderId = "user-abc",
        morseContent = ".- / -... -.-.",
        decodedContent = "A BC",
        timestampMs = 1_700_000_000_000L,
        wpm = 20,
    )

    @Test
    fun serializationRoundTrip() {
        val encoded = json.encodeToString(sample)
        val decoded = json.decodeFromString<MorseMessage>(encoded)
        assertEquals(sample, decoded)
    }

    @Test
    fun deserializationIgnoresUnknownKeys() {
        val withExtra = """
            {
              "id":"msg-1","senderId":"user-abc",
              "morseContent":".-","decodedContent":"A",
              "timestampMs":1000,"wpm":15,
              "unknownField":"ignored"
            }
        """.trimIndent()
        val msg = json.decodeFromString<MorseMessage>(withExtra)
        assertEquals("msg-1", msg.id)
        assertEquals(15, msg.wpm)
    }

    @Test
    fun allFieldsPreserved() {
        val encoded = json.encodeToString(sample)
        val decoded = json.decodeFromString<MorseMessage>(encoded)
        assertEquals("msg-1", decoded.id)
        assertEquals("user-abc", decoded.senderId)
        assertEquals(".- / -... -.-.", decoded.morseContent)
        assertEquals("A BC", decoded.decodedContent)
        assertEquals(1_700_000_000_000L, decoded.timestampMs)
        assertEquals(20, decoded.wpm)
    }
}
