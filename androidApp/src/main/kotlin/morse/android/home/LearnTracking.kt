package morse.android.home

import morse.android.persistence.StoredSession
import morse.practice.MistakeRecord
import morse.practice.Lesson
import morse.practice.ProgressTracker
import morse.practice.SessionScore
import morse.practice.TimeProvider

data class LearnTrackingState(
    val unlockedLessons: List<Lesson>,
    val weakCharacters: List<Char>,
)

object LearnTracking {

    fun buildTracker(
        sessions: List<StoredSession>,
        lessons: List<Lesson>,
        timeProvider: TimeProvider,
    ): LearnTrackingState {
        val tracker = ProgressTracker(timeProvider, lessons)
        val lessonsById = lessons.associateBy { it.id }
        sessions.forEach { stored ->
            val lesson = lessonsById[stored.lessonId] ?: return@forEach
            tracker.recordSession(
                lesson = lesson,
                score = SessionScore(stored.correct, stored.total, stored.wpm),
                mistakes = stored.mistakes.mapNotNull { mistake ->
                    mistake.character.firstOrNull()?.let { MistakeRecord(it, mistake.count) }
                },
                recordedAtEpochMillis = stored.recordedAtEpochMillis,
            )
        }
        return LearnTrackingState(
            unlockedLessons = tracker.getUnlockedLessons(),
            weakCharacters = tracker.getWeakCharacters(),
        )
    }
}
