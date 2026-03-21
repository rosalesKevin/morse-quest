package morse.communication

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommunicationSessionTest {

    private fun message(id: String) = MorseMessage(
        id = id,
        senderId = "u1",
        morseContent = ".-",
        decodedContent = "A",
        timestampMs = 0L,
        wpm = 20,
    )

    @Test
    fun newSessionHasNoMessagesOrParticipants() {
        val session = CommunicationSession(sessionId = "s1", localUserId = "u1")
        assertTrue(session.messages.isEmpty())
        assertTrue(session.participants.isEmpty())
    }

    @Test
    fun withMessageAppendsInReceiveOrder() {
        val session = CommunicationSession(sessionId = "s1", localUserId = "u1")
        val updated = session.withMessage(message("m1")).withMessage(message("m2"))
        assertEquals(listOf("m1", "m2"), updated.messages.map { it.id })
    }

    @Test
    fun withParticipantAddsNewParticipant() {
        val session = CommunicationSession(sessionId = "s1", localUserId = "u1")
        val updated = session.withParticipant("u2")
        assertTrue("u2" in updated.participants)
    }

    @Test
    fun withParticipantIsIdempotent() {
        val session = CommunicationSession(sessionId = "s1", localUserId = "u1")
            .withParticipant("u2")
            .withParticipant("u2")
        assertEquals(1, session.participants.size)
    }

    @Test
    fun sessionIsImmutable() {
        val original = CommunicationSession(sessionId = "s1", localUserId = "u1")
        val updated = original.withMessage(message("m1"))
        assertTrue(original.messages.isEmpty())
        assertFalse(updated.messages.isEmpty())
    }
}
