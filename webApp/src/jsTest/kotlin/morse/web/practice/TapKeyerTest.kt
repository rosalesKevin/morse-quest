package morse.web.practice

import morse.core.TimingEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TapKeyerTest {

    @Test
    fun classifiesPressLengthsAndBuildsCanonicalMorse() {
        val timing = TimingEngine(characterWpm = 20, effectiveWpm = 20)
        val keyer = TapKeyer(timing)

        keyer.press(timestampMs = 0L)
        keyer.release(timestampMs = timing.dotDurationMs)
        assertEquals(".", keyer.currentAnswer)
        assertEquals('.', keyer.lastFeedback?.symbol)

        val nextPressAt = timing.dotDurationMs + timing.letterGapMs + 1L
        keyer.press(timestampMs = nextPressAt)
        keyer.release(timestampMs = nextPressAt + timing.dashDurationMs)

        assertEquals(". -", keyer.currentAnswer)
        assertEquals('-', keyer.lastFeedback?.symbol)
    }

    @Test
    fun flushesDecodedPreviewThroughSharedTapParser() {
        val timing = TimingEngine(characterWpm = 20, effectiveWpm = 20)
        val keyer = TapKeyer(timing)

        keyer.press(timestampMs = 0L)
        keyer.release(timestampMs = timing.dotDurationMs)

        val nextPressAt = timing.dotDurationMs + timing.intraCharacterGapMs
        keyer.press(timestampMs = nextPressAt)
        keyer.release(timestampMs = nextPressAt + timing.dashDurationMs)

        val decoded = keyer.idle(timestampMs = nextPressAt + timing.dashDurationMs + (timing.wordGapMs * 3))

        assertEquals(".-", keyer.currentAnswer)
        assertEquals("A", decoded)
        assertNotNull(keyer.lastFeedback)
    }
}
