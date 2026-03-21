package morse.android.audiodecode

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import morse.core.AudioToneEvent
import morse.core.TimingEngine
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import morse.android.util.MainDispatcherRule

class AudioDecodeViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val timing = TimingEngine(characterWpm = 20, effectiveWpm = 20)

    private fun viewModel(capture: IAudioCapture = IdleCapture()): AudioDecodeViewModel =
        AudioDecodeViewModel(timing, capture)

    // ── initial state ───────────────────────────────────────────────────

    @Test
    fun initialStateIsIdle() = runTest {
        viewModel().uiState.test {
            val state = awaitItem()
            assertFalse(state.isListening)
            assertEquals("", state.morseText)
            assertEquals("", state.decodedText)
            assertFalse(state.permissionDenied)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── listening lifecycle ──────────────────────────────────────────────

    @Test
    fun startListeningTransitionsToListening() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.startListening()
            val state = awaitItem()
            assertTrue(state.isListening)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun stopListeningTransitionsToNotListening() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.startListening()
            skipItems(1) // isListening = true
            vm.stopListening()
            val state = awaitItem()
            assertFalse(state.isListening)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── permission ───────────────────────────────────────────────────────

    @Test
    fun permissionDeniedUpdatesState() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.onPermissionDenied()
            val state = awaitItem()
            assertTrue(state.permissionDenied)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun permissionGrantedClearsPermissionDenied() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.onPermissionDenied()
            val denied = awaitItem()
            assertTrue(denied.permissionDenied)
            // onPermissionGranted only clears the flag; startListening is called separately from UI
            vm.onPermissionGranted()
            val cleared = awaitItem()
            assertFalse(cleared.permissionDenied)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── reset ────────────────────────────────────────────────────────────

    @Test
    fun resetClearsMorseAndDecodedText() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.reset()
            val state = awaitItem()
            assertEquals("", state.morseText)
            assertEquals("", state.decodedText)
            assertFalse(state.isListening)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── sensitivity ───────────────────────────────────────────────────────

    @Test
    fun updateSensitivityReflectsInState() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.updateSensitivity(0.12f)
            val state = awaitItem()
            assertEquals(0.12f, state.sensitivity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── error state ───────────────────────────────────────────────────────

    @Test
    fun captureErrorUpdatesErrorState() = runTest {
        val vm = viewModel(ErrorCapture(RuntimeException("mic error")))
        vm.uiState.test {
            skipItems(1) // initial state
            vm.startListening()
            val listening = awaitItem()  // isListening = true
            assertTrue(listening.isListening)
            val errorState = awaitItem() // error emitted when coroutine catches exception
            assertFalse(errorState.isListening)
            assertTrue(
                errorState.error?.contains("mic error") == true,
                "Expected error containing 'mic error' but got: ${errorState.error}"
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}

// ── test doubles ──────────────────────────────────────────────────────────────

/** Never emits — simulates no audio activity */
private class IdleCapture : IAudioCapture {
    override fun start(): Flow<AudioToneEvent> = flow { /* never emits */ }
    override fun stop() = Unit
}

/** Throws immediately to simulate capture failure */
private class ErrorCapture(private val error: Throwable) : IAudioCapture {
    override fun start(): Flow<AudioToneEvent> = flow { throw error }
    override fun stop() = Unit
}
