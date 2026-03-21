package morse.web.persistence

import morse.practice.LessonCatalog
import morse.practice.MistakeRecord
import morse.practice.SessionScore
import morse.practice.TimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class WebProgressRepositoryTest {

    @Test
    fun recordsSessionsPersistsThemAndRebuildsTracker() {
        val driver = InMemoryStorageDriver()
        val lessons = LessonCatalog.defaultLessons()
        val timeProvider = TimeProvider { 86_400_000L }
        val repository = WebProgressRepository(
            storage = BrowserStorage(driver),
            lessons = lessons,
            timeProvider = timeProvider,
        )

        repository.recordSession(
            lesson = lessons.first(),
            score = SessionScore(correct = 8, total = 10, wpm = 15.0),
            mistakes = listOf(MistakeRecord('K', 1)),
            recordedAtEpochMillis = 86_400_000L,
        )
        repository.recordSession(
            lesson = lessons.first(),
            score = SessionScore(correct = 8, total = 10, wpm = 15.0),
            mistakes = emptyList(),
            recordedAtEpochMillis = 86_400_000L,
        )

        val reloaded = WebProgressRepository(
            storage = BrowserStorage(driver),
            lessons = lessons,
            timeProvider = timeProvider,
        )

        assertEquals(2, reloaded.loadSessions().size)

        val tracker = reloaded.buildTracker()
        assertEquals(2, tracker.getUnlockedLessons().size)
        assertEquals(80.0, tracker.getOverallAccuracy())
        assertEquals(1, tracker.getStreakDays())
    }

    private class InMemoryStorageDriver : StorageDriver {
        private val values = mutableMapOf<String, String>()

        override fun get(key: String): String? = values[key]

        override fun set(key: String, value: String) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}
