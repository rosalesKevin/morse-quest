package morse.web.communication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import morse.core.MorseDecoder
import morse.core.TimingEngine
import morse.web.practice.HoldFeedback
import morse.web.practice.TapKeyer

class CommunicationPageState(
    private val gateway: CommunicationGateway,
) {
    private val tapKeyer = TapKeyer(TimingEngine())

    val isAvailable: Boolean
        get() = gateway.isAvailable

    val isConnected: Boolean
        get() = gateway.isConnected

    val statusMessage: String
        get() = gateway.statusMessage

    val history: List<CommunicationHistoryEntry>
        get() = gateway.history

    val localUserId: String
        get() = gateway.localUserId

    var serverUrl by mutableStateOf("ws://localhost:8080")
        private set

    var sessionId by mutableStateOf("")
        private set

    var morseDraft by mutableStateOf("")
        private set

    var decodedDraft by mutableStateOf("")
        private set

    var lastTapFeedback by mutableStateOf<HoldFeedback?>(null)
        private set

    fun updateServerUrl(value: String) {
        serverUrl = value
    }

    fun updateSessionId(value: String) {
        sessionId = value
    }

    fun connect() {
        gateway.connect(serverUrl = serverUrl, sessionId = sessionId)
    }

    fun disconnect() {
        gateway.disconnect()
    }

    fun pressKey(timestampMs: Long) {
        tapKeyer.press(timestampMs)
        syncDraft()
    }

    fun releaseKey(timestampMs: Long) {
        tapKeyer.release(timestampMs)
        syncDraft()
    }

    fun idleKey(timestampMs: Long): String? {
        val decoded = tapKeyer.idle(timestampMs)
        if (decoded != null) {
            decodedDraft = decoded
        }
        syncDraft()
        return decoded
    }

    fun clearDraft() {
        tapKeyer.reset()
        morseDraft = ""
        decodedDraft = ""
        lastTapFeedback = null
    }

    fun sendDraft(wpm: Int = 20) {
        val morse = morseDraft.trim()
        val decoded = decodedDraft.ifBlank { MorseDecoder.decode(morse) }.trim().uppercase()
        if (morse.isBlank() || decoded.isBlank()) {
            return
        }
        gateway.send(decodedText = decoded, morseText = morse, wpm = wpm)
        clearDraft()
    }

    private fun syncDraft() {
        morseDraft = tapKeyer.currentAnswer
        lastTapFeedback = tapKeyer.lastFeedback
    }
}
