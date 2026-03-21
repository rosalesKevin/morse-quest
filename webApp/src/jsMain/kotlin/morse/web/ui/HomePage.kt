package morse.web.ui

import androidx.compose.runtime.Composable
import morse.web.home.HomePageState
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun HomePage(
    state: HomePageState,
    onOpenLearn: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenReference: () -> Unit,
    onOpenCommunication: () -> Unit,
) {
    Div(attrs = { classes("hero-grid") }) {
        Panel(extraClasses = "hero-card") {
            Div(attrs = { classes("eyebrow") }) { Text("Phase 06") }
            H2(attrs = { classes("hero-title") }) { Text("Learn Morse in the browser with shared KMP logic") }
            P(attrs = { classes("hero-copy") }) {
                Text("Use the lesson catalog, practice session engine, reference table, and browser audio from a desktop-first web shell.")
            }
            Div(attrs = { classes("hero-actions") }) {
                ActionButton("Open Learn", onClick = onOpenLearn)
                ActionButton("Quick Practice", kind = "secondary", onClick = onOpenPractice)
                ActionButton("Reference", kind = "secondary", onClick = onOpenReference)
                ActionButton("Communicate", kind = "secondary", onClick = onOpenCommunication)
            }
        }

        Panel(title = "Session Snapshot") {
            Div(attrs = { classes("stats-grid") }) {
                StatPill("Day streak", state.stats.streakDays.toString())
                StatPill("Lessons", "${state.stats.unlockedLessons}/${state.stats.totalLessons}")
                StatPill("Accuracy", formatAccuracy(state.stats.overallAccuracy))
            }
            Div(attrs = { classes("demo-card") }) {
                Div(attrs = { classes("demo-label") }) { Text("Quick demo") }
                Div(attrs = { classes("demo-code") }) { Text("SOS   ... --- ...") }
                ActionButton("Play Demo", onClick = { state.playQuickDemo() })
            }
        }
    }
}
