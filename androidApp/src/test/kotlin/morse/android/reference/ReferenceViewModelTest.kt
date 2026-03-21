package morse.android.reference

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import morse.android.audio.FakeAudioPlayer
import morse.android.haptics.FakeHapticsController
import morse.android.persistence.FakeSettingsRepository
import morse.android.util.MainDispatcherRule
import morse.core.MorseAlphabet
import morse.core.TimingEngine
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReferenceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel() = ReferenceViewModel(
        audioPlayer = FakeAudioPlayer(),
        hapticsController = FakeHapticsController(),
        settingsRepository = FakeSettingsRepository(),
        timingEngine = TimingEngine(),
    )

    @Test
    fun `initial state shows all characters`() = runTest {
        viewModel().uiState.test {
            val state = awaitItem()
            assertEquals(MorseAlphabet.characters.size, state.entries.size)
            assertEquals("", state.query)
        }
    }

    @Test
    fun `filtering by A returns only A entry`() = runTest {
        val vm = viewModel()
        vm.updateQuery("A")
        vm.uiState.test {
            val state = awaitItem()
            assertTrue(state.entries.all { it.character.toString().equals("A", ignoreCase = true) })
        }
    }

    @Test
    fun `clearing query restores all entries`() = runTest {
        val vm = viewModel()
        vm.updateQuery("A")
        vm.updateQuery("")
        vm.uiState.test {
            assertEquals(MorseAlphabet.characters.size, awaitItem().entries.size)
        }
    }
}
