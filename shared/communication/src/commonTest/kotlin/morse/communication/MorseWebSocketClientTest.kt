package morse.communication

import app.cash.turbine.test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// ---------------------------------------------------------------------------
// Test doubles
// ---------------------------------------------------------------------------

private class FakeWebSocketTransport : WebSocketTransport {
    private val frameChannel = Channel<String>(Channel.UNLIMITED)
    val sent = mutableListOf<String>()
    var closeCalled = false

    override val incoming: Flow<String> = frameChannel.receiveAsFlow()

    override suspend fun send(text: String) {
        sent.add(text)
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
    var failOnOpen: Boolean = false,
) : WebSocketTransportFactory {
    var openCount = 0

    override suspend fun open(url: String): WebSocketTransport {
        openCount++
        if (failOnOpen) throw Exception("connection refused")
        return transport
    }
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

class MorseWebSocketClientTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun message(id: String = "m1") = MorseMessage(
        id = id,
        senderId = "u1",
        morseContent = ".-",
        decodedContent = "A",
        timestampMs = 1000L,
        wpm = 20,
    )

    @Test
    fun initialStateIsDisconnected() = runTest {
        val factory = FakeWebSocketTransportFactory()
        val client = MorseWebSocketClient(
            serverUrl = "ws://localhost",
            transportFactory = factory,
            scope = this,
        )
        assertIs<ConnectionState.Disconnected>(client.connectionState.value)
    }

    @Test
    fun connectTransitionsToConnected() = runTest {
        val factory = FakeWebSocketTransportFactory()
        val client = MorseWebSocketClient(
            serverUrl = "ws://localhost",
            transportFactory = factory,
            scope = this,
        )

        client.connectionState.test {
            assertIs<ConnectionState.Disconnected>(awaitItem())
            client.connect(sessionId = "s1", userId = "u1")
            assertIs<ConnectionState.Connecting>(awaitItem())
            assertIs<ConnectionState.Connected>(awaitItem())
            client.disconnect()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun incomingMessageIsEmittedToFlow() = runTest {
        val factory = FakeWebSocketTransportFactory()
        val client = MorseWebSocketClient(
            serverUrl = "ws://localhost",
            transportFactory = factory,
            scope = this,
        )

        client.messages.test {
            client.connect(sessionId = "s1", userId = "u1")
            // With UnconfinedTestDispatcher the connect coroutine runs eagerly and
            // suspends at the incoming.collect — transport is now in Connected state.
            factory.transport.deliver(json.encodeToString(message("m1")))
            val received = awaitItem()
            assertEquals("m1", received.id)
            client.disconnect()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun sendSerializesMessageToTransport() = runTest {
        val factory = FakeWebSocketTransportFactory()
        val client = MorseWebSocketClient(
            serverUrl = "ws://localhost",
            transportFactory = factory,
            scope = this,
        )

        client.connectionState.test {
            assertIs<ConnectionState.Disconnected>(awaitItem())
            client.connect(sessionId = "s1", userId = "u1")
            assertIs<ConnectionState.Connecting>(awaitItem())
            assertIs<ConnectionState.Connected>(awaitItem())

            val msg = message("out-1")
            client.send(msg)

            assertEquals(1, factory.transport.sent.size)
            val decoded = json.decodeFromString<MorseMessage>(factory.transport.sent.first())
            assertEquals("out-1", decoded.id)

            client.disconnect()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun disconnectTransitionsToDisconnected() = runTest {
        val factory = FakeWebSocketTransportFactory()
        val client = MorseWebSocketClient(
            serverUrl = "ws://localhost",
            transportFactory = factory,
            scope = this,
        )

        client.connectionState.test {
            assertIs<ConnectionState.Disconnected>(awaitItem())
            client.connect(sessionId = "s1", userId = "u1")
            assertIs<ConnectionState.Connecting>(awaitItem())
            assertIs<ConnectionState.Connected>(awaitItem())
            client.disconnect()
            assertIs<ConnectionState.Disconnected>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun failureAfterMaxRetriesTransitionsToFailed() = runTest {
        val factory = FakeWebSocketTransportFactory(failOnOpen = true)
        val client = MorseWebSocketClient(
            serverUrl = "ws://localhost",
            transportFactory = factory,
            scope = this,
            maxRetries = 1,
        )

        client.connectionState.test {
            assertIs<ConnectionState.Disconnected>(awaitItem())
            client.connect(sessionId = "s1", userId = "u1")
            assertIs<ConnectionState.Connecting>(awaitItem())   // attempt 1
            assertIs<ConnectionState.Disconnected>(awaitItem()) // backoff gap
            assertIs<ConnectionState.Connecting>(awaitItem())   // attempt 2
            assertIs<ConnectionState.Failed>(awaitItem())       // retries exhausted
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(2, factory.openCount)
    }

    @Test
    fun reconnectAfterDisconnectOpensNewTransport() = runTest {
        val factory = FakeWebSocketTransportFactory()
        val client = MorseWebSocketClient(
            serverUrl = "ws://localhost",
            transportFactory = factory,
            scope = this,
        )

        client.connectionState.test {
            // First connect/disconnect cycle
            assertIs<ConnectionState.Disconnected>(awaitItem())
            client.connect(sessionId = "s1", userId = "u1")
            assertIs<ConnectionState.Connecting>(awaitItem())
            assertIs<ConnectionState.Connected>(awaitItem())
            client.disconnect()
            assertIs<ConnectionState.Disconnected>(awaitItem())

            // Second connect cycle
            client.connect(sessionId = "s1", userId = "u1")
            assertIs<ConnectionState.Connecting>(awaitItem())
            assertIs<ConnectionState.Connected>(awaitItem())
            client.disconnect()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(2, factory.openCount)
    }

    @Test
    fun sendIsNoOpWhenDisconnected() = runTest {
        val factory = FakeWebSocketTransportFactory()
        val client = MorseWebSocketClient(
            serverUrl = "ws://localhost",
            transportFactory = factory,
            scope = this,
        )
        // Not connected — send should not throw
        client.send(message())
        assertTrue(factory.transport.sent.isEmpty())
    }

    @Test
    fun urlIncludesSessionIdAndUserId() = runTest {
        var capturedUrl = ""
        val factory = object : WebSocketTransportFactory {
            override suspend fun open(url: String): WebSocketTransport {
                capturedUrl = url
                return FakeWebSocketTransport()
            }
        }
        val client = MorseWebSocketClient(
            serverUrl = "ws://host:8080",
            transportFactory = factory,
            scope = this,
        )

        client.connectionState.test {
            assertIs<ConnectionState.Disconnected>(awaitItem())
            client.connect(sessionId = "room42", userId = "alice")
            assertIs<ConnectionState.Connecting>(awaitItem())
            assertIs<ConnectionState.Connected>(awaitItem())
            client.disconnect()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals("ws://host:8080/session/room42/connect?userId=alice", capturedUrl)
    }
}
