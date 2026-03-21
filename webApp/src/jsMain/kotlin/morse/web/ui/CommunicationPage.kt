package morse.web.ui

import androidx.compose.runtime.Composable
import morse.web.communication.CommunicationPageState
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.events.SyntheticKeyboardEvent

@Composable
fun CommunicationPage(state: CommunicationPageState) {
    Div(attrs = { classes("practice-layout") }) {
        Panel(title = "Connection") {
            Div(attrs = { classes("status-banner") }) { Text(state.statusMessage) }
            P(attrs = { classes("muted-copy") }) {
                Text("Use the shared Phase 5 WebSocket client from the browser. Identity stays local in browser storage so reconnects reuse the same anonymous user id.")
            }
            Div(attrs = { classes("connection-grid") }) {
                Div {
                    Label(forId = "server-url") { Text("Server URL") }
                    Input(type = InputType.Text, attrs = {
                        id("server-url")
                        classes("answer-input")
                        value(state.serverUrl)
                        onInput { event -> state.updateServerUrl(event.value) }
                    })
                }
                Div {
                    Label(forId = "session-id") { Text("Session ID") }
                    Input(type = InputType.Text, attrs = {
                        id("session-id")
                        classes("answer-input")
                        value(state.sessionId)
                        onInput { event -> state.updateSessionId(event.value) }
                    })
                }
            }
            Div(attrs = { classes("practice-actions") }) {
                ActionButton("Connect", onClick = { state.connect() })
                ActionButton("Disconnect", kind = "secondary", onClick = { state.disconnect() })
            }
            Div(attrs = { classes("feedback-chip") }) { Text("Local user: ${state.localUserId}") }
        }

        Panel(title = "Keyboard Morse Draft") {
            state.lastTapFeedback?.let { feedback ->
                Div(attrs = { classes("feedback-chip") }) {
                    Text("Last hold: ${feedback.durationMs}ms -> ${if (feedback.symbol == '.') "dot" else "dash"}")
                }
            }
            Div(attrs = {
                classes("tap-surface")
                attr("tabindex", "0")
                onKeyDown { event -> handleCommunicationKeyDown(event, state) }
                onKeyUp { event -> handleCommunicationKeyUp(event, state) }
            }) {
                Text("Focus here and use Space to build a Morse draft")
            }
            Div(attrs = { classes("practice-actions") }) {
                ActionButton("Press", kind = "secondary", onClick = { state.pressKey(nowMs()) })
                ActionButton("Release", kind = "secondary", onClick = { state.releaseKey(nowMs()) })
                ActionButton("Decode Draft", kind = "ghost", onClick = { state.idleKey(nowMs() + 10_000L) })
                ActionButton("Send Draft", onClick = { state.sendDraft() })
                ActionButton("Clear", kind = "ghost", onClick = { state.clearDraft() })
            }
            Div(attrs = { classes("draft-grid") }) {
                Div(attrs = { classes("draft-card") }) {
                    Div(attrs = { classes("draft-label") }) { Text("Morse") }
                    Div(attrs = { classes("draft-value") }) { Text(if (state.morseDraft.isBlank()) "..." else state.morseDraft) }
                }
                Div(attrs = { classes("draft-card") }) {
                    Div(attrs = { classes("draft-label") }) { Text("Decoded") }
                    Div(attrs = { classes("draft-value") }) { Text(if (state.decodedDraft.isBlank()) "Waiting for flush" else state.decodedDraft) }
                }
            }
        }

        Panel(title = "Message History") {
            if (state.history.isEmpty()) {
                P(attrs = { classes("muted-copy") }) { Text("No messages yet. Connect to a session, key a draft, and send it through the shared client.") }
            } else {
                state.history.forEach { entry ->
                    Div(attrs = { classes("history-row") }) {
                        Div(attrs = { classes("draft-label") }) { Text(entry.direction) }
                        Div(attrs = { classes("history-message") }) {
                            Div(attrs = { classes("draft-value") }) { Text(entry.decodedText) }
                            Div(attrs = { classes("history-meta") }) { Text("${entry.morseText} · ${entry.wpm} WPM") }
                        }
                    }
                }
            }
        }
    }
}

private fun handleCommunicationKeyDown(event: SyntheticKeyboardEvent, state: CommunicationPageState) {
    if (event.key == " " || event.key == "Spacebar") {
        event.preventDefault()
        state.pressKey(nowMs())
    }
}

private fun handleCommunicationKeyUp(event: SyntheticKeyboardEvent, state: CommunicationPageState) {
    if (event.key == " " || event.key == "Spacebar") {
        event.preventDefault()
        state.releaseKey(nowMs())
    }
}
