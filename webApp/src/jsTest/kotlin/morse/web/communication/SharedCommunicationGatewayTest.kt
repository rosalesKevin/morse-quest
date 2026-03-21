package morse.web.communication

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import morse.communication.MorseMessage
import morse.communication.MorseWebSocketClient
import morse.communication.WebSocketTransport
import morse.communication.WebSocketTransportFactory
import morse.practice.TimeProvider
import morse.web.InMemoryStorageDriver
import morse.web.persistence.BrowserStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SharedCommunicationGatewayTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun usesSharedClientPersistsIdentityAndTracksHistory() = runTest {
        val transportFactory = FakeWebSocketTransportFactory()
        val storageDriver = InMemoryStorageDriver()
        val gateway = SharedCommunicationGateway(
            storage = BrowserStorage(storageDriver),
            scope = this,
            timeProvider = TimeProvider { 1_000L },
            transportFactoryProvider = { transportFactory },
            clientFactory = { serverUrl: String, factory: WebSocketTransportFactory, scope: kotlinx.coroutines.CoroutineScope ->
                MorseWebSocketClient(serverUrl, factory, scope, maxRetries = 0)
            },
        )

        gateway.connect("ws://localhost:8080", "room-1")
        advanceUntilIdle()

        assertTrue(gateway.isAvailable)
        assertTrue(gateway.isConnected)
        assertTrue(gateway.statusMessage.contains("Connected", ignoreCase = true))

        gateway.send(decodedText = "A", morseText = ".-", wpm = 22)
        advanceUntilIdle()

        assertEquals(1, gateway.history.size)
        val outbound = json.decodeFromString<MorseMessage>(transportFactory.transport.sent.single())
        assertEquals(".-", outbound.morseContent)
        assertEquals("A", outbound.decodedContent)

        transportFactory.transport.deliver(
            json.encodeToString(
                MorseMessage(
                    id = "remote-1",
                    senderId = "peer",
                    morseContent = "-...",
                    decodedContent = "B",
                    timestampMs = 2_000L,
                    wpm = 18,
                )
            )
        )
        advanceUntilIdle()

        assertEquals(2, gateway.history.size)
        assertEquals("Peer", gateway.history.last().direction)
        assertEquals(gateway.localUserId, storageDriver.get("communication_user_id"))

        val secondGateway = SharedCommunicationGateway(
            storage = BrowserStorage(storageDriver),
            scope = this,
            timeProvider = TimeProvider { 2_000L },
            transportFactoryProvider = { transportFactory },
            clientFactory = { serverUrl: String, factory: WebSocketTransportFactory, scope: kotlinx.coroutines.CoroutineScope ->
                MorseWebSocketClient(serverUrl, factory, scope, maxRetries = 0)
            },
        )
        assertEquals(gateway.localUserId, secondGateway.localUserId)

        gateway.disconnect()
        advanceUntilIdle()
        assertFalse(gateway.isConnected)
    }

    private class FakeWebSocketTransport : WebSocketTransport {
        private val frameChannel = Channel<String>(Channel.UNLIMITED)
        val sent = mutableListOf<String>()
        var closeCalled = false

        override val incoming: Flow<String> = frameChannel.receiveAsFlow()

        override suspend fun send(text: String) {
            sent += text
        }

        override suspend fun close() {
            closeCalled = true
            frameChannel.close()
        }

        fun deliver(text: String) {
            frameChannel.trySend(text)
        }
    }

    private class FakeWebSocketTransportFactory(
        val transport: FakeWebSocketTransport = FakeWebSocketTransport(),
    ) : WebSocketTransportFactory {
        override suspend fun open(url: String): WebSocketTransport = transport
    }
}
