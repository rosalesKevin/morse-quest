package morse.android.freestyle

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import morse.android.audio.FakeAudioPlayer
import morse.android.haptics.FakeHapticsController
import morse.android.persistence.FakeSettingsRepository
import morse.android.practice.GapType
import morse.android.util.MainDispatcherRule
import morse.core.TimingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FreestyleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val timing = TimingEngine(20, 20)
    private val fakeAudio = FakeAudioPlayer()
    private val fakeHaptics = FakeHapticsController()
    private val fakeSettings = FakeSettingsRepository()

    private fun viewModel() = FreestyleViewModel(
        settingsRepository = fakeSettings,
        timingEngine = timing,
        audioPlayer = fakeAudio,
        hapticsController = fakeHaptics,
    )

    // ---- onGapElapsed / text accumulation ----

    @Test
    fun `onGapElapsed LETTER with single dot appends E to decodedText`() = runTest {
        val vm = viewModel()
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)  // dot
        vm.onGapElapsed(GapType.LETTER)
        assertEquals("E", vm.decodedText.value)
    }

    @Test
    fun `onGapElapsed LETTER clears touchpadState after commit`() = runTest {
        val vm = viewModel()
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)
        vm.onGapElapsed(GapType.LETTER)
        assertTrue(vm.touchpadState.letterGroups.isEmpty())
    }

    @Test
    fun `successive letter commits accumulate decoded text`() = runTest {
        val vm = viewModel()
        // E = "."
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)
        vm.onGapElapsed(GapType.LETTER)
        // T = "-"
        vm.touchpadState.recordPress(timing.dashDurationMs * 2)
        vm.onGapElapsed(GapType.LETTER)
        assertEquals("ET", vm.decodedText.value)
    }

    @Test
    fun `word gap inserts space before next letter`() = runTest {
        val vm = viewModel()
        // E
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)
        vm.onGapElapsed(GapType.LETTER)
        // simulate word gap elapsed
        vm.onWordGapElapsed()
        // T
        vm.touchpadState.recordPress(timing.dashDurationMs * 2)
        vm.onGapElapsed(GapType.LETTER)
        assertEquals("E T", vm.decodedText.value)
    }

    @Test
    fun `unrecognized pattern appends question mark`() = runTest {
        val vm = viewModel()
        // press six dashes — not a valid character
        repeat(6) { vm.touchpadState.recordPress(timing.dashDurationMs * 2) }
        vm.onGapElapsed(GapType.LETTER)
        assertEquals("?", vm.decodedText.value)
    }

    // ---- onDeleteLast ----

    @Test
    fun `onDeleteLast removes last in-progress symbol when buffer not empty`() = runTest {
        val vm = viewModel()
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)  // dot pending
        vm.onDeleteLast()
        assertTrue(vm.touchpadState.letterGroups.isEmpty())
    }

    @Test
    fun `onDeleteLast removes last char from decodedText when buffer empty`() = runTest {
        val vm = viewModel()
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)
        vm.onGapElapsed(GapType.LETTER)  // commits "E", clears touchpadState
        vm.onDeleteLast()                // buffer empty → remove from canvas
        assertEquals("", vm.decodedText.value)
    }

    @Test
    fun `onDeleteLast on empty state does nothing`() = runTest {
        val vm = viewModel()
        vm.onDeleteLast()  // no crash, no state change
        assertEquals("", vm.decodedText.value)
    }

    // ---- onClearAll ----

    @Test
    fun `onClearAll resets decodedText and touchpadState`() = runTest {
        val vm = viewModel()
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)
        vm.onGapElapsed(GapType.LETTER)
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)
        vm.onClearAll()
        assertEquals("", vm.decodedText.value)
        assertTrue(vm.touchpadState.letterGroups.isEmpty())
    }

    // ---- audio/haptic feedback ----

    @Test
    fun `onTap plays sidetone audio signal`() = runTest {
        val vm = viewModel()
        vm.onTap(timing.dotDurationMs / 2)
        assertTrue(fakeAudio.playedSignals.isNotEmpty())
    }

    @Test
    fun `onTap triggers haptic when hapticsEnabled`() = runTest {
        fakeSettings.updateHapticsEnabled(true)
        val fakeHapticsRecording = object : morse.android.haptics.IHapticsController {
            val calls = mutableListOf<List<morse.core.Signal>>()
            override fun vibrateSignals(signals: List<morse.core.Signal>) { calls += signals }
            override fun cancel() {}
        }
        val vm2 = FreestyleViewModel(fakeSettings, timing, fakeAudio, fakeHapticsRecording)
        vm2.onTap(timing.dotDurationMs / 2)
        assertTrue(fakeHapticsRecording.calls.isNotEmpty())
    }

    @Test
    fun `onTap skips haptic when hapticsEnabled is false`() = runTest {
        fakeSettings.updateHapticsEnabled(false)
        val fakeHapticsRecording = object : morse.android.haptics.IHapticsController {
            val calls = mutableListOf<List<morse.core.Signal>>()
            override fun vibrateSignals(signals: List<morse.core.Signal>) { calls += signals }
            override fun cancel() {}
        }
        val vm = FreestyleViewModel(fakeSettings, timing, fakeAudio, fakeHapticsRecording)
        vm.onTap(timing.dotDurationMs / 2)
        assertTrue(fakeHapticsRecording.calls.isEmpty())
    }

    // ---- restoreText (undo support) ----

    @Test
    fun `restoreText sets decodedText to provided value`() = runTest {
        val vm = viewModel()
        vm.restoreText("HELLO")
        assertEquals("HELLO", vm.decodedText.value)
    }

    @Test
    fun `restoreText overwrites existing decodedText`() = runTest {
        val vm = viewModel()
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)
        vm.onGapElapsed(GapType.LETTER)  // "E"
        vm.restoreText("WORLD")
        assertEquals("WORLD", vm.decodedText.value)
    }

    @Test
    fun `restoreText resets pending word space so next letter has no leading space`() = runTest {
        val vm = viewModel()
        // Commit "E", which starts the word-gap timer and the flag could be set
        vm.touchpadState.recordPress(timing.dotDurationMs / 2)
        vm.onGapElapsed(GapType.LETTER)
        // Simulate word-gap timer firing BEFORE restoreText
        vm.onWordGapElapsed()   // pendingWordSpace = true
        // Undo via restoreText — should clear pendingWordSpace
        vm.restoreText("HELLO")
        // Commit "T" — should NOT have a leading space because restoreText cleared the flag
        vm.touchpadState.recordPress(timing.dashDurationMs * 2)
        vm.onGapElapsed(GapType.LETTER)
        assertEquals("HELLOT", vm.decodedText.value)
    }
}
