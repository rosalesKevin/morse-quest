package morse.android.home

import morse.android.persistence.StoredSession
import morse.android.persistence.StoredMistake
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
    fun `build surfaces weak characters and most recently practiced lesson as quick practice target`() {
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
        assertEquals(lessons[0].title, summary.quickPracticeLessonTitle)
    }

    @Test
    fun `quickPracticeLessonId returns most recently practiced lesson when sessions exist`() {
        val sessions = listOf(
            storedSession(dayOffset = -2, lessonId = lessons[0].id, correct = 8, total = 10, wpm = 18.0),
            storedSession(dayOffset = -1, lessonId = lessons[2].id, correct = 9, total = 10, wpm = 22.0),
            storedSession(dayOffset = 0,  lessonId = lessons[1].id, correct = 7, total = 10, wpm = 20.0),
        )

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        // Most recent session (dayOffset = 0) was on lessons[1]
        assertEquals(lessons[1].id, summary.quickPracticeLessonId)
    }

    @Test
    fun `quickPracticeLessonTitle matches the title of the most recently practiced lesson`() {
        val sessions = listOf(
            storedSession(dayOffset = 0, lessonId = lessons[1].id, correct = 9, total = 10, wpm = 20.0),
        )

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals(lessons[1].title, summary.quickPracticeLessonTitle)
    }

    @Test
    fun `quickPracticeLessonTitle is empty when session references unknown lessonId`() {
        val sessions = listOf(
            storedSession(dayOffset = 0, lessonId = "lesson-does-not-exist", correct = 9, total = 10, wpm = 20.0),
        )

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals("", summary.quickPracticeLessonTitle)
        assertEquals("lesson-does-not-exist", summary.quickPracticeLessonId)
    }

    @Test
    fun `quickPracticeLessonId falls back to first unlocked lesson when no sessions exist`() {
        val summary = HomeSummaryCalculator.build(
            sessions = emptyList(),
            lessons = lessons,
            timeProvider = timeProvider,
        )

        // No sessions → first unlocked lesson (lesson-1 is always unlocked)
        assertEquals(lessons[0].id, summary.quickPracticeLessonId)
        assertEquals(lessons[0].title, summary.quickPracticeLessonTitle)
    }

    @Test
    fun `home summary no longer exposes deprecated recommended level`() {
        val fieldNames = HomeSummary::class.java.declaredFields.map { it.name }

        assertFalse(fieldNames.contains("recommendedLevel"))
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
