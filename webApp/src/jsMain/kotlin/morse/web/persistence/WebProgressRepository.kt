package morse.web.persistence

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import morse.practice.Lesson
import morse.practice.MistakeRecord
import morse.practice.ProgressTracker
import morse.practice.SessionScore
import morse.practice.TimeProvider

class WebProgressRepository(
    private val storage: BrowserStorage,
    private val lessons: List<Lesson>,
    private val timeProvider: TimeProvider,
    private val json: Json = Json,
) {
    fun loadSessions(): List<WebStoredSession> {
        val raw = storage.getString(KEY) ?: return emptyList()
        return runCatching { json.decodeFromString<List<WebStoredSession>>(raw) }.getOrElse { emptyList() }
    }

    fun recordSession(
        lesson: Lesson,
        score: SessionScore,
        mistakes: List<MistakeRecord>,
        recordedAtEpochMillis: Long = timeProvider.currentEpochMillis(),
    ) {
        val updated = loadSessions() + WebStoredSession.from(
            lessonId = lesson.id,
            score = score,
            mistakes = mistakes,
            recordedAtEpochMillis = recordedAtEpochMillis,
        )
        storage.setString(KEY, json.encodeToString(updated))
    }

    fun clearAll() {
        storage.remove(KEY)
    }

    fun buildTracker(): ProgressTracker {
        val tracker = ProgressTracker(timeProvider, lessons)
        val lessonById = lessons.associateBy { it.id }
        loadSessions().forEach { stored ->
            val lesson = lessonById[stored.lessonId] ?: return@forEach
            val validMistakes = stored.mistakes.mapNotNull { mistake ->
                val character = mistake.character.firstOrNull() ?: return@mapNotNull null
                MistakeRecord(character = character, count = mistake.count)
            }
            tracker.recordSession(
                lesson = lesson,
                score = SessionScore(stored.correct, stored.total, stored.wpm),
                mistakes = validMistakes,
                recordedAtEpochMillis = stored.recordedAtEpochMillis,
            )
        }
        return tracker
    }

    private companion object {
        private const val KEY = "web_progress_sessions"
    }
}
