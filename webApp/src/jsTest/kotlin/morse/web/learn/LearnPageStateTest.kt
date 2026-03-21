package morse.web.learn

import morse.practice.MistakeRecord
import morse.practice.SessionScore
import morse.web.createTestDependencies
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LearnPageStateTest {

    @Test
    fun refreshReflectsUnlockedLessonsFromSharedProgressRules() {
        val dependencies = createTestDependencies()
        val state = LearnPageState(
            lessons = dependencies.lessons,
            progressRepository = dependencies.progressRepository,
            settingsRepository = dependencies.settingsRepository,
            audioPlayer = dependencies.audioPlayer,
        )

        assertEquals(1, state.lessonItems.count { it.isUnlocked })
        assertTrue(state.lessonItems.first().isUnlocked)

        dependencies.progressRepository.recordSession(
            lesson = dependencies.lessons.first(),
            score = SessionScore(correct = 8, total = 10, wpm = 15.0),
            mistakes = listOf(MistakeRecord('K', 1)),
        )
        dependencies.progressRepository.recordSession(
            lesson = dependencies.lessons.first(),
            score = SessionScore(correct = 8, total = 10, wpm = 15.0),
            mistakes = emptyList(),
        )

        state.refresh()

        assertEquals(2, state.lessonItems.count { it.isUnlocked })
    }
}
