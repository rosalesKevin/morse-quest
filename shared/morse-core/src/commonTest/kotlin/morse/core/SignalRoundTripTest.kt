package morse.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SignalRoundTripTest {
    private val timingEngine = TimingEngine()
    private val encoder = MorseEncoder(timingEngine)

    @Test
    fun encodesSignalsWithoutTrailingGap() {
        val signals = encoder.encodeToSignals("EE")

        assertFalse(signals.last().type == SignalType.LETTER_GAP)
        assertEquals(
            listOf(SignalType.DOT, SignalType.LETTER_GAP, SignalType.DOT),
            signals.map { it.type },
        )
    }

    @Test
    fun roundTripsSignalsBackToNormalizedText() {
        val signals = timingEngine.textToSignals("HELLO WORLD")

        assertEquals("HELLO WORLD", MorseDecoder.decodeSignals(signals))
        assertEquals("HELLO WORLD", timingEngine.signalsToText(signals))
    }
}
