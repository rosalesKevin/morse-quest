package morse.android.home

import kotlin.math.roundToInt
import morse.android.persistence.StoredSession
import morse.practice.Lesson
import morse.practice.TimeProvider

data class HomeSummary(
    val quickPracticeLessonId: String,
    val quickPracticeLessonTitle: String,
    val bestWpm: Int,
    val bestAccuracy: Double,
    val longestStreakDays: Int,
    val focusCharacters: List<Char>,
)

object HomeSummaryCalculator {

    fun build(
        sessions: List<StoredSession>,
        lessons: List<Lesson>,
        timeProvider: TimeProvider,
    ): HomeSummary {
        val tracker = LearnTracking.buildTracker(sessions, lessons, timeProvider)
        val bestSession = sessions.maxByOrNull { accuracy(it) }

        val quickPracticeLessonId = sessions
            .maxByOrNull { it.recordedAtEpochMillis }
            ?.lessonId
            ?: (tracker.unlockedLessons.firstOrNull()?.id ?: lessons.firstOrNull()?.id.orEmpty())

        val quickPracticeLessonTitle = lessons
            .firstOrNull { it.id == quickPracticeLessonId }
            ?.title
            .orEmpty()

        return HomeSummary(
            quickPracticeLessonId = quickPracticeLessonId,
            quickPracticeLessonTitle = quickPracticeLessonTitle,
            bestWpm = sessions.maxOfOrNull { it.wpm.roundToInt() } ?: 0,
            bestAccuracy = bestSession?.let(::accuracy) ?: 0.0,
            longestStreakDays = longestStreakDays(sessions),
            focusCharacters = tracker.weakCharacters.take(3),
        )
    }

    private fun accuracy(session: StoredSession): Double =
        if (session.total == 0) 0.0 else (session.correct.toDouble() / session.total.toDouble()) * 100.0

    private fun longestStreakDays(sessions: List<StoredSession>): Int {
        val days = sessions.map { it.recordedAtEpochMillis / DAY_MS }.distinct().sorted()
        if (days.isEmpty()) {
            return 0
        }

        var best = 1
        var current = 1
        for (index in 1 until days.size) {
            if (days[index] == days[index - 1] + 1) {
                current++
                best = maxOf(best, current)
            } else {
                current = 1
            }
        }
        return best
    }

    private const val DAY_MS = 86_400_000L
}
