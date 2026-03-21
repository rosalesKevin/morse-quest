package morse.android.practice

import morse.core.TimingEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class TouchpadStateTest {

    private fun createState(wpm: Int = 20) = TouchpadState(TimingEngine(wpm, wpm))

    @Test
    fun `short press produces dot`() {
        val state = createState()
        state.recordPress(50L) // well under dash threshold
        assertEquals(".", state.answer)
    }

    @Test
    fun `long press produces dash`() {
        val state = createState()
        val dashThreshold = state.dotDashThresholdMs
        state.recordPress(dashThreshold + 10)
        assertEquals("-", state.answer)
    }

    @Test
    fun `multiple presses concatenate within letter`() {
        val state = createState()
        state.recordPress(50L) // dot
        state.recordPress(200L) // dash
        assertEquals(".-", state.answer)
    }

    @Test
    fun `clear resets state`() {
        val state = createState()
        state.recordPress(50L)
        state.clear()
        assertEquals("", state.answer)
    }

    @Test
    fun `deleteLast removes last symbol`() {
        val state = createState()
        state.recordPress(50L) // dot
        state.recordPress(200L) // dash
        state.deleteLast()
        assertEquals(".", state.answer)
    }

    @Test
    fun `deleteLast on empty is no-op`() {
        val state = createState()
        state.deleteLast() // should not crash
        assertEquals("", state.answer)
    }

    @Test
    fun `dotDashThreshold is midpoint between dot and dash`() {
        val engine = TimingEngine(20, 20)
        val state = TouchpadState(engine)
        val expected = (engine.dotDurationMs + engine.dashDurationMs) / 2
        assertEquals(expected, state.dotDashThresholdMs)
    }

    @Test
    fun `letter gap threshold is 5x dash duration`() {
        val engine = TimingEngine(20, 20)
        val state = TouchpadState(engine)
        assertEquals(engine.dashDurationMs * 5, state.letterGapThresholdMs)
    }

    @Test
    fun `word gap threshold is 10x dash duration`() {
        val engine = TimingEngine(20, 20)
        val state = TouchpadState(engine)
        assertEquals(engine.dashDurationMs * 10, state.wordGapThresholdMs)
    }

    @Test
    fun `onGapElapsed with letter gap inserts letter break`() {
        val state = createState()
        state.recordPress(50L) // dot
        state.onGapElapsed(GapType.LETTER)
        state.recordPress(50L) // dot in new letter
        assertEquals(". .", state.answer)
    }

    @Test
    fun `onGapElapsed with word gap inserts word separator`() {
        val state = createState()
        state.recordPress(50L)
        state.onGapElapsed(GapType.WORD)
        state.recordPress(50L)
        assertEquals(". / .", state.answer)
    }

    @Test
    fun `onGapElapsed on empty state is no-op`() {
        val state = createState()
        state.onGapElapsed(GapType.LETTER) // should not crash
        assertEquals("", state.answer)
    }
}
