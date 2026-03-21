package morse.android.learn

import morse.android.persistence.StoredSession
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Test
import kotlin.test.assertEquals

class LessonProgressMapperTest {

    private val lessons = LessonCatalog.defaultLessons()

    @Test
    fun `map marks lessons as mastered in progress and locked`() {
        val sessions = listOf(
            storedSession(lessonId = lessons[0].id, correct = 8, total = 10),
            storedSession(lessonId = lessons[0].id, correct = 9, total = 10),
            storedSession(lessonId = lessons[1].id, correct = 6, total = 10),
        )

        val items = LessonProgressMapper.map(
            sessions = sessions,
            lessons = lessons,
            timeProvider = TimeProvider { 0L },
        )

        assertEquals(LessonVisualState.Mastered, items[0].visualState)
        assertEquals(LessonVisualState.InProgress, items[1].visualState)
        assertEquals(LessonVisualState.Locked, items[2].visualState)
    }

    @Test
    fun `map makes review available after passing second lesson and keeps next standard locked`() {
        val sessions = listOf(
            storedSession(lessonId = lessons[0].id, correct = 8, total = 10),
            storedSession(lessonId = lessons[1].id, correct = 8, total = 10),
        )

        val items = LessonProgressMapper.map(
            sessions = sessions,
            lessons = lessons,
            timeProvider = TimeProvider { 0L },
        )

        assertEquals(LessonVisualState.Available, items[2].visualState)
        assertEquals(LessonVisualState.Locked, items[3].visualState)
    }

    @Test
    fun `map unlocks next standard only after review node is passed above seventy five percent`() {
        val sessions = listOf(
            storedSession(lessonId = lessons[0].id, correct = 8, total = 10),
            storedSession(lessonId = lessons[1].id, correct = 8, total = 10),
            storedSession(lessonId = lessons[2].id, correct = 8, total = 10),
        )

        val items = LessonProgressMapper.map(
            sessions = sessions,
            lessons = lessons,
            timeProvider = TimeProvider { 0L },
        )

        assertEquals(LessonVisualState.InProgress, items[2].visualState)
        assertEquals(LessonVisualState.Available, items[3].visualState)
    }

    @Test
    fun `map returns best accuracy percent for unlocked lessons`() {
        val sessions = listOf(
            storedSession(lessonId = lessons[0].id, correct = 7, total = 10),
            storedSession(lessonId = lessons[0].id, correct = 9, total = 10),
        )

        val items = LessonProgressMapper.map(
            sessions = sessions,
            lessons = lessons,
            timeProvider = TimeProvider { 0L },
        )

        assertEquals(90, items[0].masteryPercent)
    }

    private fun storedSession(
        lessonId: String,
        correct: Int,
        total: Int,
    ) = StoredSession(
        lessonId = lessonId,
        correct = correct,
        total = total,
        wpm = 18.0,
        mistakes = emptyList(),
        recordedAtEpochMillis = 0L,
    )
}
