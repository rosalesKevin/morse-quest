package morse.android.home

import morse.android.persistence.StoredSession
import morse.android.persistence.StoredMistake
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Test
import kotlin.test.assertEquals

class HomeSummaryCalculatorTest {

    private val lessons = LessonCatalog.defaultLessons()
    private val today = 86_400_000L * 100
    private val timeProvider = TimeProvider { today }

    @Test
    fun `build returns personal bests and longest streak from session history`() {
        val sessions = listOf(
            storedSession(dayOffset = -3, lessonId = lessons[0].id, correct = 6, total = 10, wpm = 16.0),
            storedSession(dayOffset = -2, lessonId = lessons[0].id, correct = 8, total = 10, wpm = 18.0),
            storedSession(dayOffset = -1, lessonId = lessons[1].id, correct = 9, total = 10, wpm = 22.0),
            storedSession(dayOffset = 0, lessonId = lessons[1].id, correct = 10, total = 10, wpm = 20.0),
        )

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals(4, summary.longestStreakDays)
        assertEquals(22, summary.bestWpm)
        assertEquals(100.0, summary.bestAccuracy)
    }

    @Test
    fun `build surfaces weak characters and first unlocked lesson as quick practice target`() {
        val sessions = listOf(
            storedSession(dayOffset = 0, lessonId = lessons[0].id, correct = 4, total = 10, wpm = 18.0),
            storedSession(
                dayOffset = 0,
                lessonId = lessons[0].id,
                correct = 3,
                total = 10,
                wpm = 18.0,
                mistakes = listOf(StoredMistake("K", 3), StoredMistake("M", 2)),
            ),
        )

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals("lesson-1", summary.quickPracticeLessonId)
        assertEquals('K', summary.focusCharacters.first())
    }

    private fun storedSession(
        dayOffset: Int,
        lessonId: String,
        correct: Int,
        total: Int,
        wpm: Double,
        mistakes: List<StoredMistake> = emptyList(),
    ) = StoredSession(
        lessonId = lessonId,
        correct = correct,
        total = total,
        wpm = wpm,
        mistakes = mistakes,
        recordedAtEpochMillis = today + (dayOffset * 86_400_000L),
    )
}
