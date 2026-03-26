package morse.android.home

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import morse.android.persistence.FakeProgressRepository
import morse.android.persistence.FakeSettingsRepository
import morse.android.persistence.StoredSession
import morse.android.quest.FakeDailyQuestRepository
import morse.android.util.MainDispatcherRule
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessons = LessonCatalog.defaultLessons()
    private val fixedTime = 86_400_000L * 100
    private val timeProvider = TimeProvider { fixedTime }

    @Test
    fun `initial state shows zero streak and zero accuracy`() = runTest {
        val vm = HomeViewModel(FakeProgressRepository(), FakeSettingsRepository(), FakeDailyQuestRepository(), lessons, timeProvider)
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.streakDays)
            assertEquals(0.0, state.overallAccuracy)
        }
    }

    @Test
    fun `initial state shows one unlocked lesson`() = runTest {
        val vm = HomeViewModel(FakeProgressRepository(), FakeSettingsRepository(), FakeDailyQuestRepository(), lessons, timeProvider)
        vm.uiState.test {
            assertEquals(1, awaitItem().unlockedLessonCount)
        }
    }

    @Test
    fun `total lessons matches catalog size`() = runTest {
        val vm = HomeViewModel(FakeProgressRepository(), FakeSettingsRepository(), FakeDailyQuestRepository(), lessons, timeProvider)
        vm.uiState.test {
            assertEquals(lessons.size, awaitItem().totalLessons)
        }
    }

    @Test
    fun `ui state points start practicing to the latest unlocked lesson`() = runTest {
        val sessions = listOf(
            storedSession(lessonId = lessons[0].id, correct = 8, total = 10, recordedAt = fixedTime - 86_400_000L),
            storedSession(lessonId = lessons[1].id, correct = 8, total = 10, recordedAt = fixedTime),
        )
        val vm = HomeViewModel(FakeProgressRepository(sessions = sessions), FakeSettingsRepository(), FakeDailyQuestRepository(), lessons, timeProvider)

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(lessons[2].id, state.currentPathLessonId)
            assertEquals(lessons[2].title, state.currentPathLessonTitle)
        }
    }

    @Test
    fun `ui state shows last practiced and next lesson titles`() = runTest {
        val sessions = listOf(
            storedSession(lessonId = lessons[0].id, correct = 8, total = 10, recordedAt = fixedTime - 86_400_000L),
            storedSession(lessonId = lessons[1].id, correct = 5, total = 10, recordedAt = fixedTime),
        )
        val vm = HomeViewModel(FakeProgressRepository(sessions = sessions), FakeSettingsRepository(), FakeDailyQuestRepository(), lessons, timeProvider)

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(lessons[1].title, state.lastPracticedLessonTitle)
            assertEquals(lessons[2].title, state.nextLessonTitle)
        }
    }

    @Test
    fun `home ui state no longer exposes deprecated recommended level`() {
        val fieldNames = HomeViewModel.UiState::class.java.declaredFields.map { it.name }

        assertFalse(fieldNames.contains("recommendedLevel"))
    }

    private fun storedSession(
        lessonId: String,
        correct: Int,
        total: Int,
        recordedAt: Long,
    ) = StoredSession(
        lessonId = lessonId,
        correct = correct,
        total = total,
        wpm = 20.0,
        mistakes = emptyList(),
        recordedAtEpochMillis = recordedAt,
    )
}
