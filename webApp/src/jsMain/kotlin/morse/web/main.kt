package morse.web

import morse.web.ui.WebApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        WebApp()
    }
}
