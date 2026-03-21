package morse.web.ui

import androidx.compose.runtime.Composable
import morse.web.learn.LearnLessonItem
import morse.web.learn.LearnPageState
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun LearnPage(
    state: LearnPageState,
    onStartPractice: (String) -> Unit,
) {
    val selectedLesson = state.selectedLesson

    Div(attrs = { classes("learn-layout") }) {
        Panel(title = "Lessons") {
            Div(attrs = { classes("lesson-list") }) {
                state.lessonItems.forEach { item ->
                    LessonRow(
                        item = item,
                        selected = item.lesson.id == state.selectedLessonId,
                        onSelect = { state.selectLesson(item.lesson.id) },
                    )
                }
            }
        }

        Panel(title = selectedLesson?.title ?: "Lesson detail") {
            if (selectedLesson == null) {
                P { Text("Select an unlocked lesson to see its character set and start practice.") }
            } else {
                P(attrs = { classes("muted-copy") }) {
                    Text("Introduced characters: ${selectedLesson.characters.joinToString(" ")}")
                }
                Div(attrs = { classes("character-grid") }) {
                    selectedLesson.characters.forEach { character ->
                        Div(attrs = { classes("character-card") }) {
                            Div(attrs = { classes("character-glyph") }) { Text(character.toString()) }
                            Div(attrs = { classes("character-morse") }) {
                                Text(state.referenceEntries.firstOrNull { it.character == character }?.morse ?: "")
                            }
                            ActionButton("Play", kind = "ghost", onClick = { state.playCharacter(character) })
                        }
                    }
                }
                if (state.canStartSelectedPractice()) {
                    Div(attrs = { classes("section-actions") }) {
                        ActionButton("Start Practice", onClick = { onStartPractice(selectedLesson.id) })
                    }
                }
            }
        }

        Panel(title = "Side Reference", extraClasses = "side-panel") {
            Div(attrs = { classes("reference-mini-grid") }) {
                state.referenceEntries.take(18).forEach { entry ->
                    Div(attrs = { classes("reference-mini-card") }) {
                        Span(attrs = { classes("reference-mini-char") }) { Text(entry.character.toString()) }
                        Span(attrs = { classes("reference-mini-morse") }) { Text(entry.morse) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(
    item: LearnLessonItem,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Button(attrs = {
        classes("lesson-row")
        if (selected) {
            classes("selected")
        }
        if (!item.isUnlocked) {
            classes("locked")
        }
        if (item.isUnlocked) {
            onClick { onSelect() }
        }
    }) {
        Div(attrs = { classes("lesson-row-title") }) { Text(item.lesson.title) }
        Div(attrs = { classes("lesson-row-meta") }) {
            Text(if (item.isUnlocked) item.lesson.characters.joinToString(" ") else "Locked")
        }
    }
}
