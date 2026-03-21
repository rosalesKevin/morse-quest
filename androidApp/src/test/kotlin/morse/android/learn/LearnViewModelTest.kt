package morse.android.learn

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import morse.android.audio.FakeAudioPlayer
import morse.android.haptics.FakeHapticsController
import morse.android.persistence.FakeProgressRepository
import morse.android.persistence.FakeSettingsRepository
import morse.android.util.MainDispatcherRule
import morse.core.TimingEngine
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LearnViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessons = LessonCatalog.defaultLessons()
    private val timeProvider = TimeProvider { 0L }

    private fun viewModel() = LearnViewModel(
        progressRepository = FakeProgressRepository(),
        settingsRepository = FakeSettingsRepository(),
        timingEngine = TimingEngine(),
        audioPlayer = FakeAudioPlayer(),
        hapticsController = FakeHapticsController(),
        lessons = lessons,
        timeProvider = timeProvider,
    )

    @Test
    fun `initial state shows lesson list`() = runTest {
        viewModel().uiState.test {
            assertIs<LearnViewModel.UiState.LessonList>(awaitItem())
        }
    }

    @Test
    fun `selecting a lesson switches to detail state`() = runTest {
        val vm = viewModel()
        vm.selectLesson(lessons.first())
        vm.uiState.test {
            assertIs<LearnViewModel.UiState.LessonDetail>(awaitItem())
        }
    }

    @Test
    fun `back from detail returns to list`() = runTest {
        val vm = viewModel()
        vm.selectLesson(lessons.first())
        vm.back()
        vm.uiState.test {
            assertIs<LearnViewModel.UiState.LessonList>(awaitItem())
        }
    }

    @Test
    fun `lesson list includes all lessons`() = runTest {
        viewModel().uiState.test {
            val state = awaitItem() as LearnViewModel.UiState.LessonList
            assertEquals(lessons.size, state.lessons.size)
        }
    }

    @Test
    fun `first lesson is always unlocked`() = runTest {
        viewModel().uiState.test {
            val state = awaitItem() as LearnViewModel.UiState.LessonList
            assertEquals(true, state.lessons.first().isUnlocked)
        }
    }
}
