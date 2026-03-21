package morse.web.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import morse.web.app.WebAppState
import morse.web.app.WebDependencies
import morse.web.app.WebRoute
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Footer
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Header
import org.jetbrains.compose.web.dom.Main
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun WebApp() {
    val state = remember { WebAppState(WebDependencies.default()) }

    Div(attrs = { classes("app-shell") }) {
        Header(attrs = { classes("topbar") }) {
            Div(attrs = { classes("brand-lockup") }) {
                Div(attrs = { classes("eyebrow") }) { Text("Morse Code KMP") }
                H1(attrs = { classes("brand-title") }) { Text("Browser Trainer") }
            }
            Div(attrs = { classes("nav-row") }) {
                NavButton("Home", state.route == WebRoute.Home) { state.navigate(WebRoute.Home) }
                NavButton("Learn", state.route == WebRoute.Learn) { state.navigate(WebRoute.Learn) }
                NavButton("Practice", state.route == WebRoute.Practice) { state.navigate(WebRoute.Practice) }
                NavButton("Reference", state.route == WebRoute.Reference) { state.navigate(WebRoute.Reference) }
                NavButton("Communicate", state.route == WebRoute.Communicate) { state.navigate(WebRoute.Communicate) }
            }
        }

        Main(attrs = { classes("page-shell") }) {
            when (state.route) {
                WebRoute.Home -> HomePage(
                    state = state.homePage,
                    onOpenLearn = { state.navigate(WebRoute.Learn) },
                    onOpenPractice = {
                        val lessonId = state.learnPage.lessonItems.firstOrNull { it.isUnlocked }?.lesson?.id
                            ?: state.practicePage.selectedLessonId
                            ?: "lesson-1"
                        state.openPractice(lessonId)
                    },
                    onOpenReference = { state.navigate(WebRoute.Reference) },
                    onOpenCommunication = { state.navigate(WebRoute.Communicate) },
                )

                WebRoute.Learn -> LearnPage(
                    state = state.learnPage,
                    onStartPractice = { lessonId -> state.openPractice(lessonId) },
                )

                WebRoute.Practice -> PracticePage(state = state.practicePage)
                WebRoute.Reference -> ReferencePage(state = state.referencePage)
                WebRoute.Communicate -> CommunicationPage(state = state.communicationPage)
            }
        }

        Footer(attrs = { classes("footer-note") }) {
            P {
                Text("Shared Morse and practice logic stay in KMP modules. Browser-only storage, audio, and UI remain isolated in the web app.")
            }
        }
    }
}

@Composable
private fun NavButton(label: String, active: Boolean, onClick: () -> Unit) {
    Button(attrs = {
        classes("nav-button")
        if (active) {
            classes("active")
        }
        onClick { onClick() }
    }) {
        Text(label)
    }
}
