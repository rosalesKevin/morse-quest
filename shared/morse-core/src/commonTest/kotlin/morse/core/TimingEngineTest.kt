package morse.core

import kotlin.math.roundToLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimingEngineTest {
    @Test
    fun usesItuTimingAtMatchingCharacterAndEffectiveSpeed() {
        val timing = TimingEngine(characterWpm = 20, effectiveWpm = 20)

        assertEquals(60L, timing.dotDurationMs)
        assertEquals(180L, timing.dashDurationMs)
        assertEquals(60L, timing.intraCharacterGapMs)
        assertEquals(180L, timing.letterGapMs)
        assertEquals(420L, timing.wordGapMs)
    }

    @Test
    fun supportsRuntimeSpeedChanges() {
        val timing = TimingEngine(characterWpm = 20, effectiveWpm = 20)

        timing.setSpeeds(characterWpm = 15, effectiveWpm = 15)

        assertEquals(80L, timing.dotDurationMs)
        assertEquals(240L, timing.dashDurationMs)
        assertEquals(240L, timing.letterGapMs)
        assertEquals(560L, timing.wordGapMs)
    }

    @Test
    fun stretchesSpacingForFarnsworthTiming() {
        val timing = TimingEngine(characterWpm = 20, effectiveWpm = 10)
        val expectedSpacingUnit = (
            ((50.0 * (1200.0 / 10.0)) - (31.0 * (1200.0 / 20.0))) / 19.0
            ).roundToLong()

        assertEquals(60L, timing.dotDurationMs)
        assertEquals(expectedSpacingUnit * 3L, timing.letterGapMs)
        assertEquals(expectedSpacingUnit * 7L, timing.wordGapMs)
        assertTrue(timing.letterGapMs > timing.dashDurationMs)
    }
}
