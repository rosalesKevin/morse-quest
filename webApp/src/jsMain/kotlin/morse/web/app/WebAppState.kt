package morse.web.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import morse.web.communication.CommunicationPageState
import morse.web.home.HomePageState
import morse.web.learn.LearnPageState
import morse.web.practice.PracticePageState
import morse.web.reference.ReferencePageState

class WebAppState(
    private val dependencies: WebDependencies,
) {
    var route by mutableStateOf(WebRoute.Home)
        private set

    val homePage = HomePageState(
        lessons = dependencies.lessons,
        progressRepository = dependencies.progressRepository,
        settingsRepository = dependencies.settingsRepository,
        audioPlayer = dependencies.audioPlayer,
    )

    val learnPage = LearnPageState(
        lessons = dependencies.lessons,
        progressRepository = dependencies.progressRepository,
        settingsRepository = dependencies.settingsRepository,
        audioPlayer = dependencies.audioPlayer,
    )

    val practicePage = PracticePageState(
        lessons = dependencies.lessons,
        progressRepository = dependencies.progressRepository,
        settingsRepository = dependencies.settingsRepository,
        audioPlayer = dependencies.audioPlayer,
        timeProvider = dependencies.timeProvider,
        onProgressChanged = ::refreshDerivedState,
    )

    val referencePage = ReferencePageState(
        audioPlayer = dependencies.audioPlayer,
        settingsRepository = dependencies.settingsRepository,
    )

    val communicationPage = CommunicationPageState(dependencies.communicationGateway)

    init {
        practicePage.setLesson(dependencies.lessons.firstOrNull()?.id.orEmpty())
    }

    fun navigate(nextRoute: WebRoute) {
        route = nextRoute
    }

    fun openPractice(lessonId: String) {
        practicePage.setLesson(lessonId)
        route = WebRoute.Practice
    }

    private fun refreshDerivedState() {
        homePage.refresh()
        learnPage.refresh()
    }
}
