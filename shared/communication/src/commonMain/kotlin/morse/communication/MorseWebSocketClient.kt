package morse.communication

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Shared WebSocket client for Morse communication sessions.
 *
 * Usage:
 * 1. Observe [connectionState] and [messages] before calling [connect].
 * 2. Call [connect] with the target [sessionId] and [userId].
 * 3. Call [send] to transmit a [MorseMessage] to the session.
 * 4. Call [disconnect] to cleanly shut down.
 *
 * Reconnection: on transport failure the client retries up to [maxRetries] times using
 * exponential backoff (1 s, 2 s, 4 s, 8 s, 16 s). After exhausting retries the state
 * transitions to [ConnectionState.Failed].
 *
 * @param serverUrl Base WebSocket URL, e.g. `"ws://example.com"`.
 * @param transportFactory Factory that opens a [WebSocketTransport] for a full URL.
 *   Use [ktorWebSocketTransportFactory] for production; inject a test double in tests.
 * @param scope Coroutine scope that owns the connection lifetime.
 * @param maxRetries Maximum reconnection attempts before giving up (default 5).
 */
class MorseWebSocketClient(
    private val serverUrl: String,
    private val transportFactory: WebSocketTransportFactory,
    private val scope: CoroutineScope,
    private val maxRetries: Int = 5,
) {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableSharedFlow<MorseMessage>()
    val messages: SharedFlow<MorseMessage> = _messages.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true }

    private var connectJob: Job? = null
    private val activeTransport = MutableStateFlow<WebSocketTransport?>(null)

    /**
     * Begins the connection loop. Safe to call again after [disconnect] or a [ConnectionState.Failed].
     * Any previous connection attempt is cancelled before starting a new one.
     */
    fun connect(sessionId: String, userId: String) {
        connectJob?.cancel()
        val url = "$serverUrl/session/$sessionId/connect?userId=$userId"
        connectJob = scope.launch {
            var retries = 0
            try {
                while (isActive) {
                    _connectionState.value = ConnectionState.Connecting
                    try {
                        val transport = transportFactory.open(url)
                        activeTransport.value = transport
                        _connectionState.value = ConnectionState.Connected
                        retries = 0
                        transport.incoming.collect { text ->
                            runCatching { json.decodeFromString<MorseMessage>(text) }
                                .onSuccess { _messages.emit(it) }
                        }
                        // Incoming completed without error — treat as clean close
                        break
                    } catch (e: Exception) {
                        activeTransport.value = null
                        retries++
                        if (retries > maxRetries) {
                            _connectionState.value = ConnectionState.Failed(e)
                            break
                        }
                        // Signal the gap between attempts so observers can distinguish
                        // "first connect attempt" from "waiting to reconnect".
                        _connectionState.value = ConnectionState.Disconnected
                        val backoffMs = (1L shl (retries - 1).coerceAtMost(4)) * 1000L
                        delay(backoffMs)
                    }
                }
            } finally {
                activeTransport.value?.close()
                activeTransport.value = null
                if (_connectionState.value !is ConnectionState.Failed) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    }

    /**
     * Sends a [MorseMessage] to the active session. No-op if not currently connected.
     */
    suspend fun send(message: MorseMessage) {
        activeTransport.value?.send(json.encodeToString(message))
    }

    /**
     * Cancels the connection loop and closes the active transport.
     * State transitions to [ConnectionState.Disconnected].
     */
    fun disconnect() {
        connectJob?.cancel()
        connectJob = null
    }
}
