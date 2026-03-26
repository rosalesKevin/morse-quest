package morse.android.quest

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import morse.android.audio.FakeAudioPlayer
import morse.android.persistence.FakeSettingsRepository
import morse.android.util.MainDispatcherRule
import morse.core.TimingEngine
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DailyQuestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val generator = DailyQuestGenerator()

    private fun makeVm(
        repo: FakeDailyQuestRepository = FakeDailyQuestRepository(),
    ) = DailyQuestViewModel(
        repository = repo,
        generator = generator,
        settingsRepository = FakeSettingsRepository(),
        audioPlayer = FakeAudioPlayer(),
        timingEngine = TimingEngine(),
    )

    // ── Initialization ────────────────────────────────────────────────────────

    @Test
    fun `initial state has EASY difficulty and quest is loaded`() = runTest {
        val vm = makeVm()
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(DailyQuestDifficulty.EASY, state.selectedDifficulty)
            assertTrue(state.quest != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init restores saved difficulty from repository`() = runTest {
        val repo = FakeDailyQuestRepository()
        repo.saveProgress(index = 3, difficulty = DailyQuestDifficulty.HARD)
        val vm = makeVm(repo)
        vm.uiState.test {
            val loaded = awaitItem()
            assertEquals(DailyQuestDifficulty.HARD, loaded.selectedDifficulty)
            assertEquals(3, loaded.progressIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init marks isCompleted when repository reports completed today`() = runTest {
        val repo = FakeDailyQuestRepository()
        repo.markCompletedToday()
        val vm = makeVm(repo)
        vm.uiState.test {
            val loaded = awaitItem()
            assertTrue(loaded.isCompleted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Difficulty selection ──────────────────────────────────────────────────

    @Test
    fun `onDifficultySelected regenerates quest at new difficulty`() = runTest {
        val vm = makeVm()
        vm.uiState.test {
            awaitItem() // loaded initial state
            vm.onDifficultySelected(DailyQuestDifficulty.HARD)
            val updated = awaitItem()
            assertEquals(DailyQuestDifficulty.HARD, updated.selectedDifficulty)
            assertEquals(10, updated.quest?.questions?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDifficultySelected restores progress index when difficulty matches saved`() = runTest {
        val repo = FakeDailyQuestRepository()
        repo.saveProgress(index = 4, difficulty = DailyQuestDifficulty.MEDIUM)
        val vm = makeVm(repo)
        vm.uiState.test {
            awaitItem() // loaded initial state (MEDIUM, index=4)
            vm.onDifficultySelected(DailyQuestDifficulty.EASY)
            awaitItem() // switched to EASY, index=0
            vm.onDifficultySelected(DailyQuestDifficulty.MEDIUM)
            val restored = awaitItem()
            assertEquals(4, restored.progressIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDifficultySelected resets index to 0 when difficulty does not match saved`() = runTest {
        val repo = FakeDailyQuestRepository()
        repo.saveProgress(index = 4, difficulty = DailyQuestDifficulty.MEDIUM)
        val vm = makeVm(repo)
        vm.uiState.test {
            awaitItem() // loaded initial state (MEDIUM, index=4)
            vm.onDifficultySelected(DailyQuestDifficulty.EASY)
            val updated = awaitItem()
            assertEquals(0, updated.progressIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Answer tracking ───────────────────────────────────────────────────────

    @Test
    fun `onQuestionAnswered increments correctCount for correct answer`() = runTest {
        val vm = makeVm()
        vm.uiState.test {
            awaitItem() // loaded initial state
            vm.onQuestionAnswered(index = 0, correct = true)
            val updated = awaitItem()
            assertEquals(1, updated.correctCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onQuestionAnswered does not increment correctCount for wrong answer`() = runTest {
        val vm = makeVm()
        vm.uiState.test {
            awaitItem() // loaded initial state
            vm.onQuestionAnswered(index = 1, correct = false) // index=1 changes progressIndex so state updates
            val updated = awaitItem()
            assertEquals(0, updated.correctCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Completion ────────────────────────────────────────────────────────────

    @Test
    fun `onQuestCompleted sets isCompleted to true`() = runTest {
        val vm = makeVm()
        vm.uiState.test {
            awaitItem() // loaded initial state
            vm.onQuestCompleted()
            val updated = awaitItem()
            assertTrue(updated.isCompleted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onQuestCompleted persists completion to repository`() = runTest {
        val repo = FakeDailyQuestRepository()
        val vm = makeVm(repo)
        vm.uiState.test {
            awaitItem() // loaded initial state
            assertFalse(repo.isCompletedToday())
            vm.onQuestCompleted()
            awaitItem()
            assertTrue(repo.isCompletedToday())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
