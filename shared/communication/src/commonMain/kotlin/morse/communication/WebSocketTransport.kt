package morse.communication

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Low-level abstraction over a WebSocket frame channel.
 * Isolates transport details from [MorseWebSocketClient] and enables test doubles.
 */
interface WebSocketTransport {
    /** Emits each incoming UTF-8 text frame. Completes when the connection closes. */
    val incoming: Flow<String>

    suspend fun send(text: String)

    suspend fun close()
}

/** Creates a [WebSocketTransport] for the given URL. */
fun interface WebSocketTransportFactory {
    suspend fun open(url: String): WebSocketTransport
}

/** Production [WebSocketTransportFactory] backed by a Ktor [HttpClient]. */
fun ktorWebSocketTransportFactory(httpClient: HttpClient): WebSocketTransportFactory =
    WebSocketTransportFactory { url ->
        val session = httpClient.webSocketSession(url)
        KtorWebSocketTransport(session)
    }

private class KtorWebSocketTransport(
    private val session: DefaultWebSocketSession,
) : WebSocketTransport {

    override val incoming: Flow<String> = flow {
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) emit(frame.readText())
            }
        } catch (_: ClosedReceiveChannelException) {
            // Connection closed normally — let the flow complete
        }
    }

    override suspend fun send(text: String) {
        session.send(Frame.Text(text))
    }

    override suspend fun close() {
        session.close()
    }
}
