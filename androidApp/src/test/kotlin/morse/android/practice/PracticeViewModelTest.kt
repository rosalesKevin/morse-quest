package morse.android.practice

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import morse.android.audio.FakeAudioPlayer
import morse.android.haptics.FakeHapticsController
import morse.android.persistence.FakeProgressRepository
import morse.android.persistence.FakeSettingsRepository
import morse.android.persistence.UserSettings
import morse.android.util.MainDispatcherRule
import morse.core.MorseDecoder
import morse.core.TimingEngine
import morse.practice.Exercise
import morse.practice.LessonCatalog
import morse.practice.TimeProvider
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessons = LessonCatalog.defaultLessons()
    private val lesson = lessons.first()

    private data class TestSubject(
        val viewModel: PracticeViewModel,
        val audioPlayer: FakeAudioPlayer,
        val settingsRepository: FakeSettingsRepository,
    )

    private fun createSubject(
        savedState: Map<String, Any?> = mapOf("lessonId" to lesson.id),
        initialSettings: UserSettings = UserSettings(),
    ): TestSubject {
        val audioPlayer = FakeAudioPlayer()
        val settingsRepository = FakeSettingsRepository(initialSettings)
        return TestSubject(
            viewModel = PracticeViewModel(
                savedStateHandle = SavedStateHandle(savedState),
                progressRepository = FakeProgressRepository(),
                settingsRepository = settingsRepository,
                timingEngine = TimingEngine(),
                audioPlayer = audioPlayer,
                hapticsController = FakeHapticsController(),
                lessons = lessons,
                timeProvider = TimeProvider { 0L },
            ),
            audioPlayer = audioPlayer,
            settingsRepository = settingsRepository,
        )
    }

    private fun viewModel() = createSubject().viewModel

    @Test
    fun `initial audio exercise plays its decoded morse timing`() = runTest {
        val subject = createSubject()
        val expectedExercise = lesson.exercises.first() as Exercise.ListenAndIdentify

        advanceUntilIdle()

        assertEquals(
            TimingEngine().textToSignals(MorseDecoder.decode(expectedExercise.morse)),
            subject.audioPlayer.playedSignals.single(),
        )
    }

    @Test
    fun `decode word replay uses decoded text timing instead of re-encoding punctuation`() = runTest {
        val subject = createSubject()
        val vm = subject.viewModel

        advanceUntilIdle()
        vm.nextExercise()
        vm.nextExercise()
        vm.playCurrentExercise()
        advanceUntilIdle()

        val exercise = (lesson.exercises[2] as Exercise.DecodeWord)
        assertEquals(
            TimingEngine().textToSignals(MorseDecoder.decode(exercise.morse)),
            subject.audioPlayer.playedSignals.last(),
        )
    }

    @Test
    fun `initial state is first exercise at index 0`() = runTest {
        viewModel().uiState.test {
            val state = awaitItem()
            assertIs<PracticeViewModel.UiState.Exercise>(state)
            assertEquals(0, (state as PracticeViewModel.UiState.Exercise).index)
        }
    }

    @Test
    fun `initial exercise state exposes lesson for persistent header`() = runTest {
        val state = viewModel().uiState.value as PracticeViewModel.UiState.Exercise

        assertEquals(lesson.id, state.lesson.id)
        assertEquals(lesson.title, state.lesson.title)
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

    @Test
    fun `quick start hard uses requested playback override without rewriting saved settings`() = runTest {
        val subject = createSubject(
            savedState = mapOf(
                "mode" to "quick_start",
                "difficulty" to "hard",
                "wpm" to 12,
            ),
            initialSettings = UserSettings(wpm = 20),
        )

        advanceUntilIdle()
        subject.viewModel.playCurrentExercise()
        advanceUntilIdle()

        val state = subject.viewModel.uiState.value as PracticeViewModel.UiState.Exercise
        val speedChallenge = collectExercises(
            createSubject(
                savedState = mapOf(
                    "mode" to "quick_start",
                    "difficulty" to "hard",
                    "wpm" to 12,
                ),
                initialSettings = UserSettings(wpm = 20),
            ).viewModel,
        ).filterIsInstance<Exercise.SpeedChallenge>().first()

        assertEquals(12, speedChallenge.targetWpm)
        assertEquals(
            TimingEngine(characterWpm = 12, effectiveWpm = 12)
                .textToSignals(MorseDecoder.decode((state.exercise as Exercise.ListenAndIdentify).morse)),
            subject.audioPlayer.playedSignals.last(),
        )
        assertEquals(20, subject.settingsRepository.currentSettings.wpm)
    }

    @Test
    fun `quick start easy and hard resolve to different exercise mixes`() = runTest {
        val easy = createSubject(
            savedState = mapOf(
                "mode" to "quick_start",
                "difficulty" to "easy",
            ),
        ).viewModel
        val hard = createSubject(
            savedState = mapOf(
                "mode" to "quick_start",
                "difficulty" to "hard",
            ),
        ).viewModel

        advanceUntilIdle()

        val easyExercises = collectExercises(easy)
        val hardExercises = collectExercises(hard)

        assertEquals(10, easyExercises.size)
        assertEquals(10, hardExercises.size)
        assertTrue(hardExercises.filterIsInstance<Exercise.SpeedChallenge>().size >= easyExercises.filterIsInstance<Exercise.SpeedChallenge>().size)
        assertTrue(hardExercises.filterIsInstance<Exercise.DecodeWord>().size >= easyExercises.filterIsInstance<Exercise.DecodeWord>().size)
    }

    private fun correctAnswerFor(exercise: Exercise): String = when (exercise) {
        is Exercise.ListenAndIdentify -> exercise.answer.toString()
        is Exercise.ReadAndTap -> exercise.expectedMorse
        is Exercise.DecodeWord -> exercise.answer
        is Exercise.EncodeWord -> exercise.expectedMorse
        is Exercise.SpeedChallenge -> exercise.text
    }

    private fun collectExercises(viewModel: PracticeViewModel): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        repeat(10) { index ->
            val state = viewModel.uiState.value as PracticeViewModel.UiState.Exercise
            exercises += state.exercise
            if (index < 9) {
                viewModel.updateAnswer(correctAnswerFor(state.exercise))
                viewModel.submitAnswer()
                viewModel.nextExercise()
            }
        }
        return exercises
    }

    @Test
    fun `hints start visible for all exercise types`() = runTest {
        val vm = viewModel()
        assertTrue(vm.isHintVisible(Exercise.ListenAndIdentify::class))
        assertTrue(vm.isHintVisible(Exercise.ReadAndTap::class))
        assertTrue(vm.isHintVisible(Exercise.DecodeWord::class))
        assertTrue(vm.isHintVisible(Exercise.EncodeWord::class))
        assertTrue(vm.isHintVisible(Exercise.SpeedChallenge::class))
    }

    @Test
    fun `hint collapses after first correct answer for that type`() = runTest {
        val vm = viewModel()
        assertTrue(vm.isHintVisible(Exercise.ListenAndIdentify::class))

        val state = vm.uiState.value as PracticeViewModel.UiState.Exercise
        val correctAnswer = correctAnswerFor(state.exercise)
        vm.updateAnswer(correctAnswer)
        vm.submitAnswer()

        assertFalse(vm.isHintVisible(state.exercise::class))
    }

    @Test
    fun `hint for one type does not affect other types`() = runTest {
        val vm = viewModel()
        val state = vm.uiState.value as PracticeViewModel.UiState.Exercise
        val correctAnswer = correctAnswerFor(state.exercise)
        vm.updateAnswer(correctAnswer)
        vm.submitAnswer()

        // Other types should still be visible
        assertTrue(vm.isHintVisible(Exercise.ReadAndTap::class))
        assertTrue(vm.isHintVisible(Exercise.EncodeWord::class))
    }
}
