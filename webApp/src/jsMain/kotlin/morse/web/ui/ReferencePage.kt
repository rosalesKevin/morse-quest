package morse.web.ui

import androidx.compose.runtime.Composable
import morse.web.reference.ReferencePageState
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text

@Composable
fun ReferencePage(state: ReferencePageState) {
    Panel(title = "Reference Table") {
        Input(type = InputType.Text, attrs = {
            classes("answer-input")
            value(state.query)
            onInput { event -> state.updateQuery(event.value) }
            attr("placeholder", "Filter by letter or pattern")
        })
        Div(attrs = { classes("reference-table") }) {
            state.entries.forEach { entry ->
                Div(attrs = { classes("reference-row") }) {
                    Div(attrs = { classes("reference-char") }) { Text(entry.character.toString()) }
                    Div(attrs = { classes("reference-pattern") }) { Text(entry.morse) }
                    ActionButton("Play", kind = "ghost", onClick = { state.playCharacter(entry.character) })
                }
            }
        }
    }
}
