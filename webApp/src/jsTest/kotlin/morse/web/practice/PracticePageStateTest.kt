package morse.web.practice

import morse.practice.SessionScore
import morse.web.createTestDependencies
import morse.web.singleExerciseLesson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PracticePageStateTest {

    @Test
    fun completesLessonAndRecordsProgress() {
        val lesson = singleExerciseLesson()
        val dependencies = createTestDependencies(lessons = listOf(lesson))
        val state = PracticePageState(
            lessons = dependencies.lessons,
            progressRepository = dependencies.progressRepository,
            settingsRepository = dependencies.settingsRepository,
            audioPlayer = dependencies.audioPlayer,
            timeProvider = dependencies.timeProvider,
        )

        state.setLesson(lesson.id)
        state.updateAnswer(".")
        state.submitAnswer()
        assertTrue(state.result?.isCorrect == true)

        state.nextExercise()

        val summary = assertNotNull(state.summary)
        assertEquals(SessionScore(correct = 1, total = 1, wpm = 0.0), summary.score)
        assertEquals(1, dependencies.progressRepository.loadSessions().size)
        assertEquals(100.0, dependencies.progressRepository.buildTracker().getOverallAccuracy())
    }
}
