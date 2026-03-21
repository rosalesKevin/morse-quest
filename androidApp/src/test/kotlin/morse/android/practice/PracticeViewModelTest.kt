package morse.android.practice

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import morse.android.audio.FakeAudioPlayer
import morse.android.haptics.FakeHapticsController
import morse.android.persistence.FakeProgressRepository
import morse.android.persistence.FakeSettingsRepository
import morse.android.util.MainDispatcherRule
import morse.core.TimingEngine
import morse.practice.Exercise
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessons = LessonCatalog.defaultLessons()
    private val lesson = lessons.first()

    private fun viewModel() = PracticeViewModel(
        savedStateHandle = SavedStateHandle(mapOf("lessonId" to lesson.id)),
        progressRepository = FakeProgressRepository(),
        settingsRepository = FakeSettingsRepository(),
        timingEngine = TimingEngine(),
        audioPlayer = FakeAudioPlayer(),
        hapticsController = FakeHapticsController(),
        lessons = lessons,
        timeProvider = TimeProvider { 0L },
    )

    @Test
    fun `initial state is first exercise at index 0`() = runTest {
        viewModel().uiState.test {
            val state = awaitItem()
            assertIs<PracticeViewModel.UiState.Exercise>(state)
            assertEquals(0, (state as PracticeViewModel.UiState.Exercise).index)
        }
    }

    @Test
    fun `total exercise count matches lesson exercises`() = runTest {
        viewModel().uiState.test {
            val state = awaitItem() as PracticeViewModel.UiState.Exercise
            assertEquals(lesson.exercises.size, state.total)
        }
    }

    @Test
    fun `submitting correct answer marks result as correct`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            val exercise = (awaitItem() as PracticeViewModel.UiState.Exercise).exercise
            val correctAnswer = correctAnswerFor(exercise)
            vm.updateAnswer(correctAnswer)
            vm.submitAnswer()
            assertTrue((awaitItem() as PracticeViewModel.UiState.Exercise).result!!.isCorrect)
        }
    }

    @Test
    fun `completing all exercises transitions to summary`() = runTest {
        val vm = viewModel()
        repeat(lesson.exercises.size) {
            vm.submitAnswer()
            vm.nextExercise()
        }
        vm.uiState.test {
            assertIs<PracticeViewModel.UiState.Summary>(awaitItem())
        }
    }

    private fun correctAnswerFor(exercise: Exercise): String = when (exercise) {
        is Exercise.ListenAndIdentify -> exercise.answer.toString()
        is Exercise.ReadAndTap -> exercise.expectedMorse
        is Exercise.DecodeWord -> exercise.answer
        is Exercise.EncodeWord -> exercise.expectedMorse
        is Exercise.SpeedChallenge -> exercise.text
    }
}
