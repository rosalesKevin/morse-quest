package morse.android.home

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import morse.android.persistence.FakeProgressRepository
import morse.android.persistence.FakeSettingsRepository
import morse.android.persistence.UserSettings
import morse.android.util.MainDispatcherRule
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessons = LessonCatalog.defaultLessons()
    private val fixedTime = 86_400_000L * 100
    private val timeProvider = TimeProvider { fixedTime }

    @Test
    fun `initial state shows zero streak and zero accuracy`() = runTest {
        val vm = HomeViewModel(FakeProgressRepository(), FakeSettingsRepository(), lessons, timeProvider)
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.streakDays)
            assertEquals(0.0, state.overallAccuracy)
        }
    }

    @Test
    fun `initial state shows one unlocked lesson`() = runTest {
        val vm = HomeViewModel(FakeProgressRepository(), FakeSettingsRepository(), lessons, timeProvider)
        vm.uiState.test {
            assertEquals(1, awaitItem().unlockedLessonCount)
        }
    }

    @Test
    fun `total lessons matches catalog size`() = runTest {
        val vm = HomeViewModel(FakeProgressRepository(), FakeSettingsRepository(), lessons, timeProvider)
        vm.uiState.test {
            assertEquals(lessons.size, awaitItem().totalLessons)
        }
    }

    @Test
    fun `quick start default wpm is seeded from settings`() = runTest {
        val vm = HomeViewModel(
            FakeProgressRepository(),
            FakeSettingsRepository(UserSettings(wpm = 9)),
            lessons,
            timeProvider,
        )

        vm.uiState.test {
            assertEquals(9, awaitItem().quickStartDefaultWpm)
        }
    }
}
