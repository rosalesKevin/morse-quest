package morse.android.home

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import morse.android.persistence.StoredMistake
import morse.android.persistence.StoredSession
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Test

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
    fun `build surfaces weak characters and current path lesson from unlocked progress`() {
        val sessions = listOf(
            storedSession(dayOffset = -1, lessonId = lessons[0].id, correct = 8, total = 10, wpm = 18.0),
            storedSession(
                dayOffset = 0,
                lessonId = lessons[1].id,
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

        assertEquals(lessons[1].id, summary.currentPathLessonId)
        assertEquals(lessons[1].title, summary.currentPathLessonTitle)
        assertEquals('K', summary.focusCharacters.first())
    }

    @Test
    fun `current path lesson returns the latest unlocked lesson when sessions exist`() {
        val sessions = listOf(
            storedSession(dayOffset = -2, lessonId = lessons[0].id, correct = 8, total = 10, wpm = 18.0),
            storedSession(dayOffset = -1, lessonId = lessons[1].id, correct = 8, total = 10, wpm = 22.0),
            storedSession(dayOffset = 0, lessonId = lessons[2].id, correct = 6, total = 10, wpm = 20.0),
        )

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals(lessons[2].id, summary.currentPathLessonId)
        assertEquals(lessons[2].title, summary.currentPathLessonTitle)
    }

    @Test
    fun `last practiced lesson tracks the most recent session even when it is behind current path`() {
        val sessions = listOf(
            storedSession(dayOffset = -2, lessonId = lessons[0].id, correct = 8, total = 10, wpm = 18.0),
            storedSession(dayOffset = -1, lessonId = lessons[1].id, correct = 8, total = 10, wpm = 22.0),
            storedSession(dayOffset = 0, lessonId = lessons[1].id, correct = 5, total = 10, wpm = 20.0),
        )

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals(lessons[1].id, summary.lastPracticedLessonId)
        assertEquals(lessons[1].title, summary.lastPracticedLessonTitle)
        assertEquals(lessons[2].id, summary.currentPathLessonId)
    }

    @Test
    fun `next lesson title shows the first locked node after the current path lesson`() {
        val sessions = listOf(
            storedSession(dayOffset = -1, lessonId = lessons[0].id, correct = 8, total = 10, wpm = 18.0),
            storedSession(dayOffset = 0, lessonId = lessons[1].id, correct = 8, total = 10, wpm = 22.0),
        )

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals(lessons[3].id, summary.nextLessonId)
        assertEquals(lessons[3].title, summary.nextLessonTitle)
    }

    @Test
    fun `last practiced lesson title is empty when no session history exists`() {
        val summary = HomeSummaryCalculator.build(
            sessions = emptyList(),
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals("", summary.lastPracticedLessonId)
        assertEquals("", summary.lastPracticedLessonTitle)
    }

    @Test
    fun `current path lesson falls back to first unlocked lesson when no sessions exist`() {
        val summary = HomeSummaryCalculator.build(
            sessions = emptyList(),
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals(lessons[0].id, summary.currentPathLessonId)
        assertEquals(lessons[0].title, summary.currentPathLessonTitle)
        assertEquals(lessons[1].id, summary.nextLessonId)
        assertEquals(lessons[1].title, summary.nextLessonTitle)
    }

    @Test
    fun `next lesson title is empty when current path lesson is the final lesson`() {
        val sessions = lessons.mapIndexed { index, lesson ->
            storedSession(
                dayOffset = index - lessons.size,
                lessonId = lesson.id,
                correct = 8,
                total = 10,
                wpm = 20.0,
            )
        }

        val summary = HomeSummaryCalculator.build(
            sessions = sessions,
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals("", summary.nextLessonId)
        assertEquals("", summary.nextLessonTitle)
        assertEquals(lessons.last().id, summary.currentPathLessonId)
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
