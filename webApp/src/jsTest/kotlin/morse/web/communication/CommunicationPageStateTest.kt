package morse.web.communication

import morse.core.TimingEngine
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommunicationPageStateTest {

    @Test
    fun exposesUnavailableGatewayState() {
        val state = CommunicationPageState(UnavailableCommunicationGateway())

        assertFalse(state.isAvailable)
        assertTrue(state.statusMessage.contains("unavailable", ignoreCase = true))
    }

    @Test
    fun connectAndSendDraftDelegatesToGateway() {
        val gateway = RecordingGateway()
        val state = CommunicationPageState(gateway)
        val timing = TimingEngine()

        state.updateServerUrl("ws://example.test")
        state.updateSessionId("room-7")
        state.connect()

        assertEquals(listOf("ws://example.test" to "room-7"), gateway.connectCalls)

        state.pressKey(0L)
        state.releaseKey(timing.dotDurationMs)
        val dashPressAt = timing.dotDurationMs + timing.intraCharacterGapMs
        state.pressKey(dashPressAt)
        state.releaseKey(dashPressAt + timing.dashDurationMs)
        state.idleKey(dashPressAt + timing.dashDurationMs + (timing.wordGapMs * 3))
        state.sendDraft()

        assertEquals(listOf(Triple("A", ".-", 20)), gateway.sentMessages)
        assertEquals("", state.morseDraft)
        assertEquals("", state.decodedDraft)
    }

    private class RecordingGateway : CommunicationGateway {
        override val isAvailable: Boolean = true
        override var isConnected: Boolean = false
        override var statusMessage: String = "Disconnected"
        override var history: List<CommunicationHistoryEntry> = emptyList()
        override val localUserId: String = "web-user"
        val connectCalls = mutableListOf<Pair<String, String>>()
        val sentMessages = mutableListOf<Triple<String, String, Int>>()

        override fun connect(serverUrl: String, sessionId: String) {
            connectCalls += serverUrl to sessionId
            isConnected = true
            statusMessage = "Connected"
        }

        override fun disconnect() {
            isConnected = false
            statusMessage = "Disconnected"
        }

        override fun send(decodedText: String, morseText: String, wpm: Int) {
            sentMessages += Triple(decodedText, morseText, wpm)
        }
    }
}
