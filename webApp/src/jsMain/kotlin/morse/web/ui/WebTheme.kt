package morse.web.ui

import androidx.compose.runtime.Composable
import kotlin.math.roundToInt
import kotlin.js.Date
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text

@Composable
fun Panel(
    title: String? = null,
    extraClasses: String = "",
    content: @Composable () -> Unit,
) {
    Div(attrs = {
        classes("panel")
        if (extraClasses.isNotBlank()) {
            classes(*extraClasses.split(' ').filter { it.isNotBlank() }.toTypedArray())
        }
    }) {
        if (title != null) {
            H2(attrs = { classes("panel-title") }) { Text(title) }
        }
        content()
    }
}

@Composable
fun ActionButton(
    label: String,
    kind: String = "primary",
    onClick: () -> Unit,
) {
    Button(attrs = {
        classes("action-button", kind)
        onClick { onClick() }
    }) {
        Text(label)
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Div(attrs = { classes("stat-pill") }) {
        Div(attrs = { classes("stat-value") }) { Text(value) }
        Div(attrs = { classes("stat-label") }) { Text(label) }
    }
}

fun formatAccuracy(value: Double): String = "${value.roundToInt()}%"

fun nowMs(): Long = Date.now().toLong()
