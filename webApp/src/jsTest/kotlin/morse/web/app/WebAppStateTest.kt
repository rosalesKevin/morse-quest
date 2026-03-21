package morse.web.app

import morse.web.createTestDependencies
import kotlin.test.Test
import kotlin.test.assertEquals

class WebAppStateTest {

    @Test
    fun startsOnHomeAndCanOpenPracticeForSpecificLesson() {
        val dependencies = createTestDependencies()
        val state = WebAppState(
            WebDependencies(
                lessons = dependencies.lessons,
                settingsRepository = dependencies.settingsRepository,
                progressRepository = dependencies.progressRepository,
                audioPlayer = dependencies.audioPlayer,
                timeProvider = dependencies.timeProvider,
                communicationGateway = dependencies.communicationGateway,
            )
        )

        assertEquals(WebRoute.Home, state.route)

        state.openPractice("lesson-1")

        assertEquals(WebRoute.Practice, state.route)
        assertEquals("lesson-1", state.practicePage.selectedLessonId)
    }
}
