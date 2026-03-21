package morse.web.ui

import androidx.compose.runtime.Composable
import morse.practice.Exercise
import morse.web.practice.PracticePageState
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.events.SyntheticKeyboardEvent

@Composable
fun PracticePage(state: PracticePageState) {
    Div(attrs = { classes("practice-layout") }) {
        Panel(title = "Lesson") {
            Div(attrs = { classes("lesson-chip-row") }) {
                state.currentExercise?.let {
                    Div(attrs = { classes("exercise-type") }) { Text(exerciseLabel(it)) }
                }
                Div(attrs = { classes("exercise-type muted") }) { Text(state.selectedLessonId ?: "No lesson selected") }
            }
            state.currentExercise?.let { exercise ->
                H3 { Text(exercisePrompt(exercise)) }
                P(attrs = { classes("muted-copy") }) {
                    Text("Use the answer field or focus the tap pad and send Morse with the spacebar. The hold meter helps separate dots from dashes.")
                }
                Div(attrs = { classes("practice-actions") }) {
                    ActionButton("Replay Prompt", kind = "secondary", onClick = { state.playPrompt() })
                    ActionButton("Submit", onClick = { state.submitAnswer() })
                    if (state.result != null) {
                        ActionButton("Next", kind = "secondary", onClick = { state.nextExercise() })
                    }
                }
            } ?: run {
                val summary = state.summary
                if (summary != null) {
                    Div(attrs = { classes("summary-grid") }) {
                        StatPill("Correct", summary.score.correct.toString())
                        StatPill("Total", summary.score.total.toString())
                        StatPill("Accuracy", formatAccuracy(summary.score.accuracy))
                    }
                    if (summary.mistakes.isNotEmpty()) {
                        P { Text("Mistakes: ${summary.mistakes.joinToString { "${it.character} x${it.count}" }}") }
                    }
                } else {
                    P { Text("Choose a lesson from Home or Learn to begin practice.") }
                }
            }
        }

        Panel(title = "Answer Input") {
            Label(forId = "practice-answer") { Text("Current answer") }
            Input(type = InputType.Text, attrs = {
                id("practice-answer")
                classes("answer-input")
                value(state.answer)
                onInput { event -> state.updateAnswer(event.value) }
            })
            state.lastTapFeedback?.let { feedback ->
                Div(attrs = { classes("feedback-chip") }) {
                    Text("Last hold: ${feedback.durationMs}ms -> ${if (feedback.symbol == '.') "dot" else "dash"}")
                }
            }
            Div(attrs = {
                classes("tap-surface")
                attr("tabindex", "0")
                onKeyDown { event -> handleTapKeyDown(event, state) }
                onKeyUp { event -> handleTapKeyUp(event, state) }
            }) {
                Text("Focus here and use Space to key Morse")
            }
            Div(attrs = { classes("practice-actions") }) {
                ActionButton("Hold Key", kind = "ghost", onClick = { }) // visual fallback anchor
                ActionButton("Press", kind = "secondary", onClick = { state.pressKey(nowMs()) })
                ActionButton("Release", kind = "secondary", onClick = { state.releaseKey(nowMs()) })
                ActionButton("Flush", kind = "ghost", onClick = { state.idleKey(nowMs() + 10_000L) })
            }
        }
    }
}

private fun exerciseLabel(exercise: Exercise): String = when (exercise) {
    is Exercise.ListenAndIdentify -> "Listen and identify"
    is Exercise.ReadAndTap -> "Read and tap"
    is Exercise.DecodeWord -> "Decode word"
    is Exercise.EncodeWord -> "Encode word"
    is Exercise.SpeedChallenge -> "Speed challenge"
}

private fun exercisePrompt(exercise: Exercise): String = when (exercise) {
    is Exercise.ListenAndIdentify -> "Listen to the prompt and type the decoded character."
    is Exercise.ReadAndTap -> "Tap the Morse pattern for ${exercise.character}."
    is Exercise.DecodeWord -> "Listen to the word and type the decoded text."
    is Exercise.EncodeWord -> "Send the Morse for ${exercise.word}."
    is Exercise.SpeedChallenge -> "Copy the phrase ${exercise.text}."
}

private fun handleTapKeyDown(event: SyntheticKeyboardEvent, state: PracticePageState) {
    if (event.key == " " || event.key == "Spacebar") {
        event.preventDefault()
        state.pressKey(nowMs())
    }
}

private fun handleTapKeyUp(event: SyntheticKeyboardEvent, state: PracticePageState) {
    if (event.key == " " || event.key == "Spacebar") {
        event.preventDefault()
        state.releaseKey(nowMs())
    }
}
