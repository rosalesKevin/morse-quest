package morse.android.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import morse.practice.Lesson
import morse.practice.MistakeRecord
import morse.practice.ProgressTracker
import morse.practice.SessionScore
import morse.practice.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

interface IProgressRepository {
    val sessionHistory: Flow<List<StoredSession>>
    suspend fun recordSession(lesson: Lesson, score: SessionScore, mistakes: List<MistakeRecord>, recordedAtEpochMillis: Long)
    suspend fun clearAll()
    fun buildTracker(sessions: List<StoredSession>, lessons: List<Lesson>, timeProvider: TimeProvider): ProgressTracker
}

@Singleton
class ProgressRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : IProgressRepository {

    private val sessionsKey = stringPreferencesKey("session_history")

    override val sessionHistory: Flow<List<StoredSession>> = dataStore.data.map { prefs ->
        val json = prefs[sessionsKey] ?: return@map emptyList()
        runCatching { Json.decodeFromString<List<StoredSession>>(json) }.getOrDefault(emptyList())
    }

    override suspend fun recordSession(
        lesson: Lesson,
        score: SessionScore,
        mistakes: List<MistakeRecord>,
        recordedAtEpochMillis: Long,
    ) {
        val stored = StoredSession(
            lessonId = lesson.id,
            correct = score.correct,
            total = score.total,
            wpm = score.wpm,
            mistakes = mistakes.map { StoredMistake(it.character.toString(), it.count) },
            recordedAtEpochMillis = recordedAtEpochMillis,
        )
        dataStore.edit { prefs ->
            val existing: List<StoredSession> = prefs[sessionsKey]
                ?.let { runCatching { Json.decodeFromString<List<StoredSession>>(it) }.getOrDefault(emptyList()) }
                ?: emptyList()
            prefs[sessionsKey] = Json.encodeToString(existing + stored)
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.remove(sessionsKey) }
    }

    override fun buildTracker(
        sessions: List<StoredSession>,
        lessons: List<Lesson>,
        timeProvider: TimeProvider,
    ): ProgressTracker {
        val lessonById = lessons.associateBy { it.id }
        val tracker = ProgressTracker(timeProvider, lessons)
        for (stored in sessions) {
            val lesson = lessonById[stored.lessonId] ?: continue
            val validMistakes = stored.mistakes.mapNotNull { m ->
                val char = m.character.firstOrNull() ?: return@mapNotNull null
                MistakeRecord(char, m.count)
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
}
