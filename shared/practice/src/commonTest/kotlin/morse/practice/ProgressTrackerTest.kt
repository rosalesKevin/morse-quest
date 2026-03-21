package morse.practice

import kotlin.test.Test
import kotlin.test.assertEquals

class ProgressTrackerTest {
    private val lessons = LessonCatalog.defaultLessons().take(4)

    @Test
    fun unlocksSecondLessonAfterSingleNinetyPercentSession() {
        val tracker = ProgressTracker(FakeTimeProvider())

        tracker.recordSession(
            lesson = lessons[0],
            score = SessionScore(correct = 9, total = 10, wpm = 20.0),
            mistakes = emptyList(),
        )

        assertEquals(listOf(lessons[0], lessons[1]), tracker.getUnlockedLessons())
    }

    @Test
    fun unlocksSecondLessonAfterTwoEightyPercentSessions() {
        val tracker = ProgressTracker(FakeTimeProvider())

        repeat(2) {
            tracker.recordSession(
                lesson = lessons[0],
                score = SessionScore(correct = 8, total = 10, wpm = 20.0),
                mistakes = emptyList(),
            )
        }

        assertEquals(listOf(lessons[0], lessons[1]), tracker.getUnlockedLessons())
    }

    @Test
    fun unlocksLaterLessonsAfterThreePassingSessionsOnPreviousLesson() {
        val tracker = ProgressTracker(FakeTimeProvider())

        tracker.recordSession(lessons[0], SessionScore(9, 10, 20.0), emptyList())
        repeat(3) {
            tracker.recordSession(lessons[1], SessionScore(8, 10, 20.0), emptyList())
        }

        assertEquals(listOf(lessons[0], lessons[1], lessons[2]), tracker.getUnlockedLessons())
    }

    @Test
    fun identifiesWeakCharactersAboveTheTwentyPercentErrorRateThreshold() {
        val tracker = ProgressTracker(FakeTimeProvider())
        val lesson = Lesson(
            id = "lesson-weak",
            title = "Weak",
            characters = listOf('K', 'M'),
            exercises = listOf(
                Exercise.ListenAndIdentify("-.-", 'K'),
                Exercise.ListenAndIdentify("--", 'M'),
                Exercise.EncodeWord("KM", "-.- --"),
            ),
        )

        tracker.recordSession(
            lesson = lesson,
            score = SessionScore(1, 3, 18.0),
            mistakes = listOf(MistakeRecord('K', 1)),
        )

        assertEquals(listOf('K'), tracker.getWeakCharacters())
    }

    @Test
    fun tracksDailyStreakAcrossConsecutiveAndBrokenDates() {
        val timeProvider = FakeTimeProvider(nowEpochMillis = day(5))
        val tracker = ProgressTracker(timeProvider)

        tracker.recordSession(lessons[0], SessionScore(9, 10, 20.0), emptyList(), recordedAtEpochMillis = day(3))
        tracker.recordSession(lessons[0], SessionScore(9, 10, 20.0), emptyList(), recordedAtEpochMillis = day(4))
        tracker.recordSession(lessons[0], SessionScore(9, 10, 20.0), emptyList(), recordedAtEpochMillis = day(5))
        assertEquals(3, tracker.getStreakDays())

        tracker.recordSession(lessons[0], SessionScore(9, 10, 20.0), emptyList(), recordedAtEpochMillis = day(7))
        timeProvider.nowEpochMillis = day(7)

        assertEquals(1, tracker.getStreakDays())
    }

    @Test
    fun calculatesOverallAccuracyAcrossRecordedSessions() {
        val tracker = ProgressTracker(FakeTimeProvider())

        tracker.recordSession(lessons[0], SessionScore(9, 10, 20.0), emptyList())
        tracker.recordSession(lessons[1], SessionScore(8, 10, 20.0), emptyList())

        assertEquals(85.0, tracker.getOverallAccuracy())
    }

    private class FakeTimeProvider(
        var nowEpochMillis: Long = day(0),
    ) : TimeProvider {
        override fun currentEpochMillis(): Long = nowEpochMillis
    }

    companion object {
        private const val dayMillis: Long = 86_400_000L

        private fun day(index: Int): Long = index * dayMillis
    }
}
