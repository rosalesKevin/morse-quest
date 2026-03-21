package morse.android.persistence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import morse.practice.Lesson
import morse.practice.MistakeRecord
import morse.practice.ProgressTracker
import morse.practice.SessionScore
import morse.practice.TimeProvider

class FakeProgressRepository(
    sessions: List<StoredSession> = emptyList(),
) : IProgressRepository {
    private val _sessionHistory = MutableStateFlow(sessions)
    override val sessionHistory: Flow<List<StoredSession>> = _sessionHistory

    override suspend fun recordSession(
        lesson: Lesson,
        score: SessionScore,
        mistakes: List<MistakeRecord>,
        recordedAtEpochMillis: Long,
    ) {
        val stored = StoredSession(lesson.id, score.correct, score.total, score.wpm, emptyList(), recordedAtEpochMillis)
        _sessionHistory.value = _sessionHistory.value + stored
    }

    override suspend fun clearAll() { _sessionHistory.value = emptyList() }

    override fun buildTracker(sessions: List<StoredSession>, lessons: List<Lesson>, timeProvider: TimeProvider): ProgressTracker {
        val tracker = ProgressTracker(timeProvider, lessons)
        val lessonById = lessons.associateBy { it.id }
        for (stored in sessions) {
            val lesson = lessonById[stored.lessonId] ?: continue
            tracker.recordSession(lesson, SessionScore(stored.correct, stored.total, stored.wpm), emptyList(), stored.recordedAtEpochMillis)
        }
        return tracker
    }
}
