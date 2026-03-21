package morse.android.learn

import kotlin.math.roundToInt
import morse.android.home.LearnTracking
import morse.android.persistence.StoredSession
import morse.practice.Lesson
import morse.practice.TimeProvider

enum class LessonVisualState {
    Locked,
    Available,
    InProgress,
    Mastered,
}

data class LessonProgressItem(
    val lesson: Lesson,
    val visualState: LessonVisualState,
    val masteryPercent: Int,
)

object LessonProgressMapper {

    fun map(
        sessions: List<StoredSession>,
        lessons: List<Lesson>,
        timeProvider: TimeProvider,
    ): List<LessonProgressItem> {
        val tracking = LearnTracking.buildTracker(sessions, lessons, timeProvider)
        val scoresByLesson = sessions.groupBy { it.lessonId }

        return lessons.map { lesson ->
            val bestAccuracy = scoresByLesson[lesson.id]
                ?.maxOfOrNull { if (it.total == 0) 0.0 else (it.correct.toDouble() / it.total.toDouble()) * 100.0 }
                ?.roundToInt()
                ?: 0
            val state = when {
                lesson !in tracking.unlockedLessons -> LessonVisualState.Locked
                bestAccuracy >= 90 -> LessonVisualState.Mastered
                bestAccuracy > 0 -> LessonVisualState.InProgress
                else -> LessonVisualState.Available
            }
            LessonProgressItem(
                lesson = lesson,
                visualState = state,
                masteryPercent = bestAccuracy,
            )
        }
    }
}
