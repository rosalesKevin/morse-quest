package morse.android.practice

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import morse.core.TimingEngine
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MorseTapComposerTest {

    @Test
    fun `recordPress classifies dot and dash using timing engine thresholds`() {
        val composer = MorseTapComposer(TimingEngine(characterWpm = 20, effectiveWpm = 20))

        composer.recordPress(durationMs = 50L)
        composer.recordPress(durationMs = 250L)

        assertEquals(".-", composer.answer)
    }

    @Test
    fun `recordPress tracks hint matching and mismatch state`() {
        val composer = MorseTapComposer(
            timingEngine = TimingEngine(characterWpm = 20, effectiveWpm = 20),
            expectedPattern = ".-",
        )

        composer.recordPress(durationMs = 50L)
        composer.recordPress(durationMs = 50L)

        assertEquals(true, composer.segments[0].matchesHint)
        assertEquals(false, composer.segments[1].matchesHint)
        assertEquals(1, composer.firstMismatchIndex)
    }

    @Test
    fun `clear resets answer and visualized segments`() {
        val composer = MorseTapComposer(TimingEngine())

        composer.recordPress(durationMs = 50L)
        composer.clear()

        assertEquals("", composer.answer)
        assertEquals(emptyList(), composer.segments)
    }

    @Test
    fun `composer answer emits observable updates when presses are recorded and cleared`() = runTest {
        val composer = MorseTapComposer(TimingEngine())
        val observedAnswers = async {
            snapshotFlow { composer.answer }
                .take(3)
                .toList()
        }

        advanceUntilIdle()
        composer.recordPress(durationMs = 50L)
        advanceUntilIdle()
        composer.clear()
        advanceUntilIdle()

        assertEquals(listOf("", ".", ""), observedAnswers.await())
    }
}
