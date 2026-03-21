package morse.web.communication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import morse.communication.CommunicationSession
import morse.communication.ConnectionState
import morse.communication.MorseMessage
import morse.communication.MorseWebSocketClient
import morse.communication.UserIdentity
import morse.communication.WebSocketTransportFactory
import morse.communication.ktorWebSocketTransportFactory
import morse.practice.TimeProvider
import morse.web.persistence.BrowserStorage

data class CommunicationHistoryEntry(
    val id: String,
    val senderId: String,
    val decodedText: String,
    val morseText: String,
    val direction: String,
    val timestampMs: Long,
    val wpm: Int,
)

interface CommunicationGateway {
    val isAvailable: Boolean
    val isConnected: Boolean
    val statusMessage: String
    val history: List<CommunicationHistoryEntry>
    val localUserId: String

    fun connect(serverUrl: String, sessionId: String) = Unit
    fun disconnect() = Unit
    fun send(decodedText: String, morseText: String, wpm: Int) = Unit
}

class UnavailableCommunicationGateway : CommunicationGateway {
    override val isAvailable: Boolean = false
    override val isConnected: Boolean = false
    override val statusMessage: String = "Communication unavailable on this branch"
    override val history: List<CommunicationHistoryEntry> = emptyList()
    override val localUserId: String = "unavailable"
}

class SharedCommunicationGateway(
    private val storage: BrowserStorage,
    private val scope: CoroutineScope,
    private val timeProvider: TimeProvider,
    private val transportFactoryProvider: () -> WebSocketTransportFactory = {
        ktorWebSocketTransportFactory(
            HttpClient(Js) {
                install(WebSockets)
            }
        )
    },
    private val clientFactory: (String, WebSocketTransportFactory, CoroutineScope) -> MorseWebSocketClient =
        { serverUrl, transportFactory, clientScope ->
            MorseWebSocketClient(
                serverUrl = serverUrl,
                transportFactory = transportFactory,
                scope = clientScope,
            )
        },
) : CommunicationGateway {
    override val isAvailable: Boolean = true

    override var isConnected by mutableStateOf(false)
        private set

    override var statusMessage by mutableStateOf("Enter a server URL and session ID to connect")
        private set

    override var history by mutableStateOf(emptyList<CommunicationHistoryEntry>())
        private set

    override val localUserId: String = storage.getString(USER_ID_KEY)
        ?.takeIf { it.isNotBlank() }
        ?: UserIdentity.anonymous().userId.also { storage.setString(USER_ID_KEY, it) }

    private var activeSession = CommunicationSession(sessionId = "", localUserId = localUserId)
    private var client: MorseWebSocketClient? = null
    private var connectionJob: Job? = null
    private var messageJob: Job? = null
    private var currentSessionId: String? = null

    override fun connect(serverUrl: String, sessionId: String) {
        val normalizedServerUrl = serverUrl.trim().trimEnd('/')
        val normalizedSessionId = sessionId.trim()

        if (normalizedServerUrl.isBlank() || normalizedSessionId.isBlank()) {
            statusMessage = "Server URL and session ID are required"
            return
        }

        disconnect()
        history = emptyList()
        activeSession = CommunicationSession(sessionId = normalizedSessionId, localUserId = localUserId)
        currentSessionId = normalizedSessionId

        val sharedClient = clientFactory(normalizedServerUrl, transportFactoryProvider(), scope)
        client = sharedClient

        connectionJob = scope.launch {
            sharedClient.connectionState.collect { state ->
                isConnected = state is ConnectionState.Connected
                statusMessage = state.toStatusMessage(normalizedSessionId, localUserId)
            }
        }

        messageJob = scope.launch {
            sharedClient.messages.collect { message ->
                activeSession = activeSession
                    .withParticipant(message.senderId)
                    .withMessage(message)
                upsertHistory(message)
            }
        }

        sharedClient.connect(sessionId = normalizedSessionId, userId = localUserId)
    }

    override fun disconnect() {
        client?.disconnect()
        client = null
        connectionJob?.cancel()
        connectionJob = null
        messageJob?.cancel()
        messageJob = null
        isConnected = false
        statusMessage = currentSessionId?.let { "Disconnected from $it" } ?: "Enter a server URL and session ID to connect"
    }

    override fun send(decodedText: String, morseText: String, wpm: Int) {
        val activeClient = client
        if (activeClient == null || !isConnected) {
            statusMessage = "Connect before sending messages"
            return
        }

        val normalizedDecoded = decodedText.trim().uppercase()
        val normalizedMorse = morseText.trim()
        if (normalizedDecoded.isBlank() || normalizedMorse.isBlank()) {
            return
        }

        val message = MorseMessage(
            id = generateMessageId(),
            senderId = localUserId,
            morseContent = normalizedMorse,
            decodedContent = normalizedDecoded,
            timestampMs = timeProvider.currentEpochMillis(),
            wpm = wpm,
        )

        activeSession = activeSession
            .withParticipant(localUserId)
            .withMessage(message)
        upsertHistory(message)

        scope.launch {
            activeClient.send(message)
        }
    }

    private fun upsertHistory(message: MorseMessage) {
        val entry = CommunicationHistoryEntry(
            id = message.id,
            senderId = message.senderId,
            decodedText = message.decodedContent,
            morseText = message.morseContent,
            direction = if (message.senderId == localUserId) "You" else "Peer",
            timestampMs = message.timestampMs,
            wpm = message.wpm,
        )
        history = history
            .filterNot { it.id == entry.id }
            .plus(entry)
            .sortedBy { it.timestampMs }
    }

    private fun ConnectionState.toStatusMessage(sessionId: String, userId: String): String = when (this) {
        ConnectionState.Disconnected -> "Disconnected from $sessionId"
        ConnectionState.Connecting -> "Connecting to $sessionId as $userId"
        ConnectionState.Connected -> "Connected to $sessionId as $userId"
        is ConnectionState.Failed -> "Connection failed: ${cause?.message ?: "unknown error"}"
    }

    private fun generateMessageId(): String =
        "msg-${timeProvider.currentEpochMillis()}-${history.size + 1}"

    private companion object {
        private const val USER_ID_KEY = "communication_user_id"
    }
}
