package morse.web

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        WebApp()
    }
}

@Composable
private fun WebApp() {
    Text("Morse Code KMP")
}
